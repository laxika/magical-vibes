# Oracle data and the card modules

How card metadata is loaded, and the layout of the card and card-data modules.

## For card authors and build fixes

- A card class goes in `magical-vibes-card/{letter}`; one constructed by a card of another letter (back face, meld partner, conjure target) goes in `shared`; infrastructure in `core`. Letter modules cannot depend on each other, so a card in the wrong module fails compilation. No card module may depend on Spring.
- Card constructors hold only engine logic; `Card()` fills all metadata from the registered oracle data.
- The oracle name at a `@CardRegistration` collector number must read as the class name (`CardRegistry.verifyOracleNameMatchesClass`), or the set fails to load.
- Tests run `ON_DEMAND` (`OracleLoadMode`): `@CardUsed` preloads sets. Gradle tests prefer MTGJSON and share the repository-root `card-data-cache`.

## Loading

### A stored set is never refreshed
`set-{code}.json` pins a set's data, legality included, from its first fetch; only deleting the file, or bumping `StoredSet.FORMAT_VERSION` (which needs a new CI cache key), re-downloads it.
Owner: `SetDataStore`.
Why: oracle text must not differ between runs or providers.

### A loader reads; the registry registers
`OracleLoader.loadSet` returns data and writes nothing; every registration happens in `CardRegistry.register`.
Owner: `CardRegistry`.
Why: per-provider registration loops drift apart.

### Rules decisions about a face live in one mapper
A `SetSource` handles provider quirks and stores uninterpreted `RawFace`s; every rules decision is made in `FaceOracleMapper`, on load.
Owner: `FaceOracleMapper`.
Why: a rule written per provider diverges (a back face inheriting Defender could not attack), and a fix must not need a re-download.

### Card data is wired by the outer context
`GameEngineConfig` does not import `CardDataConfiguration`; the application and test contexts do, and the headless simulation context reuses the application's beans.
Owner: `HeadlessSimulationContext`.
Why: every set would otherwise be loaded twice.

## Split cards

### Off the stack a split card is both halves (CR 709.4)
The whole card has both names, the combined mana value and both halves' types and colours, under the `split` and `aftermath` layouts; whatever leaves the stack is the whole card.
Owner: `FaceOracleMapper.toSplitCardOracleData`; `StackEntry.getPhysicalCard`.
Why: Commit // Memory has mana value 10 everywhere but the stack.

### On the stack only the cast half exists (CR 709.3b)
Halves are the modes of one `ChooseOneEffect` in printed order; choosing mode *i* gives the spell half *i*'s characteristics, and an aftermath spell is its cast half.
Owner: `SpellCastingService.applyModeManaCost`; `StackEntry.getCard`.
Why: timing, cost and colour come from the half alone.

### Only a castable half is offered (CR 709.3a)
A cast during resolution or by permission goes through `SplitCardCastSupport`: half, X, targets, then payment, and the card returns to its zone if the cast fails. "Cast a spell with" limits test the half; "cast a card with" tests the whole card.
Owner: `SplitCardCastSupport`.
Why: the limit applies to what is actually cast.

### Fuse only from the owner's hand (CR 702.102a)
The fused mode needs `FUSE` and a cast from the owner's hand; it and any `handOnly` mode are refused elsewhere, and exile or library casts never offer it.
Owner: `SpellCastingService.validateModeCastZone`.
Why: cascade must not cast both halves.

### Either half's name matches (CR 709.4a)
Use `Card.hasName`, or `GameQueryService.permanentHasName` on the battlefield (face-down: no name; Room: unlocked doors only); name prompts offer `Card.getNames()`, never "A // B".
Owner: `Card.hasName`.
Why: a named-card effect must find a split card by one half.

### A copy stays a split card (CR 707.2)
A copy keeps the halves and the combined mana value.
Owner: `CopySupport.createCopyCard`.
Why: its halves are copiable values.
