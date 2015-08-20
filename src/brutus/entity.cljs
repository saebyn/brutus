(ns ^{:doc "Entity Manager functions for the Brutus Entity Component System"}
  brutus.entity

  (:require [goog.object]))



(defn create-uuid []
  (let [template "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
        f #(let [r (Math/floor (* (rand) 16))
                 v (if (= % \x) r (bit-or (bit-and r 0x3) 0x8))]
             (.toString v 16))]
    (.replace template (js/RegExp. "[xy]" "g") f)))


(defn create-system
  "Creates the system data structure that will need to be passed to all entity functions"
  []
  {;; Nested Map of Component Types -> Entity -> Component Instance
   :entity-components      #js {}
   ;; Map of Entities -> Set of Component Types
   :entity-component-types #js {}})


(defn create-entity
  "Create the entity and return it. Entities are just UUIDs"
  []
  (create-uuid))


(defn get-all-entities
  "Returns a list of all the entities. Not that useful in application, but good for debugging/testing"
  [system]
  (if-let [result (:entity-component-types system)]
    (goog.object/getKeys result)
    []))


(defn add-entity
  "Add the entity to the ES Data Structure and returns it"
  [{:keys [entity-component-types] :as system} entity]
  {:pre [(object? entity-component-types)]}

  (aset entity-component-types entity (array))
  system)


(defmulti get-component-type
  "Returns the type for a given component. Using a multimethod with 'type' as the dispatch-fn to allow for extensibility per application.
  By default returns the class of the component."
  type)


(defmethod get-component-type :default
  [component]
  (type component))


(defn add-component
  "Add a component instance to a given entity in the ES data structure and returns it.
  Will overwrite a component if already set."
  [{:keys [entity-components entity-component-types] :as system} entity instance]
  {:pre [(object? entity-components)
         (object? entity-component-types)]}

  (let [type (get-component-type instance)]
    (when-not (aget entity-components type)
      (aset entity-components type (js-obj)))
    (aset entity-components type entity instance)
    (.push (aget entity-component-types entity) type))
  system)


(defn get-component
  "Get the component data for a specific component type"
  [{:keys [entity-components]} entity type]
  {:pre [(object? entity-components)]}

  (aget entity-components type entity))


(defn update-component
  "Update an entity's component instance through through fn. Function is applied first with the specified component and any other args applied,
  and should return the modified component instance. Return nil if you want no change to occur."
  [system entity type fn & args]
  (if-let [update (apply fn (get-component system entity type) args)]
    (add-component system entity update)
    system))


(defn get-all-entities-with-component
  "Get all the entities that have a given component type"
  [{:keys [entity-components]} type]
  {:pre [(object? entity-components)]}

  (if-let [entities (aget entity-components type)]
    (goog.object/getKeys entities)
    []))


(defn remove-component
  "Remove a component instance from the ES data structure and returns it"
  [{:keys [entity-components entity-component-types]} entity instance]
  {:pre [(object? entity-components)
         (object? entity-component-types)]}

  (let [type (get-component-type instance)]
    (js-delete (aget entity-components type) entity)
    (js-delete (aget entity-component-types entity) type)))


(defn kill-entity
  "Destroy an entity completely from the ES data structure and returns it"
  [{:keys [entity-component-types entity-components] :as system} entity]
  {:pre [(object? entity-components)
         (object? entity-component-types)]}

  (js-delete entity-component-types entity)
  (doseq [type (goog.object/getKeys entity-components)]
    (js-delete (aget entity-components type) entity))
  
  system)


(defn get-all-components-on-entity
  "Get all the components on a specific entity. Useful for debugging"
  [{:keys [entity-component-types entity-components]} entity]
  {:pre [(object? entity-components)
         (object? entity-component-types)]}

  (let [types (aget entity-component-types entity)]
    (amap 
      types
      idx ret
      (aget entity-components (aget types idx) entity))))
