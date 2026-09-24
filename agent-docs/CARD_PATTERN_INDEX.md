# CARD_PATTERN_INDEX
| Gift token + draw/Seek replacement | `p/PoolResources.java` + `GiftEffect` + `SeekLibraryEffect` |
| end-step tapped-creature-count Seek to battlefield | `b/BuxtonDecoratedHost.java` + `ConditionalEffect` + `SeekLibraryEffect` + `ManaValueBound` |
| targeted hand exile + threshold Rat Seek with perpetual cost reduction | `t/ThoughtRattle.java` + `ChooseCardsFromTargetHandEffect` + `ConditionalEffect` + `SeekLibraryAndPerpetuallyReduceSoughtCardEffect` |
| exile opposing low-mana permanent, then owner seeks shared card type on source leaves | `d/DarkstarBanisher.java` + `ExileTargetPermanentAndTrackWithSourceEffect` + `SeekLibraryForOwnerOfCardExiledWithSourceEffect` |
| perpetual offspring grant to a hand card | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| perpetual cast-life-loss grant to nonland cards in the defending player's hand | `p/PutrefyingRotboar.java` + `PerpetuallyGiveSpellCastLifeLossToDefendingHandEffect` |
| perpetual extra mana ability grant to the topmost land card in your library | `v/VigorousFarming.java` + `PerpetuallyGiveLandExtraManaEffect` |
| perpetual random graveyard permanent conversion into a playable Food artifact | `r/ResourcefulCollector.java` + `PerpetuallyMakeRandomGraveyardPermanentFoodEffect` |
| perpetual cost reduction grant to instant and sorcery cards in hand | `c/ChargedConjuration.java` + `PerpetuallyReduceInstantAndSorceryCastCostInHandEffect` |
| perpetual cost reduction grant to creature cards in hand | `f/FountainportCharmer.java` + `PerpetuallyReduceCreatureCastCostInHandEffect` |
| perpetual +1/+1 grant to creature cards in your library | `b/BramblearmorBrawler.java` + `PerpetuallyBoostCreatureCardsInLibraryEffect` |
| perpetual +1/+1 grant to an enter-trigger source and the entering creature | `l/LeafLeapGuide.java` + `PerpetuallyBoostSourceAndEnteringCreatureEffect` |
| perpetual keyword grant to an enter-trigger source | `m/MarshlandHordemaster.java` + `PerpetuallyGrantKeywordToSourceEffect` |
| once-per-turn non-flying creature cast duplicate with perpetual flying | `a/AceFlockbringer.java` + `ConjureDuplicateOfTriggeringCreatureToHandEffect` |
| random opponent gains control of source permanent | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| Gift a named card onto an opponent's battlefield | `a/ArchivalWhorl.java` + `ConjureCardToOpponentBattlefieldEffect` |
| ETB forage conjures a named card onto your battlefield; batched Squirrel combat damage may sacrifice a token to add acorn counters | `e/EuruAcornScrounger.java` + `ConjureCardToBattlefieldEffect` |
| Rat-triggered named card conjured into a graveyard | `s/ShellfishScholar.java` + `ConjureCardToGraveyardEffect` |
| ETB named card conjured into your hand | `b/BraveMeadowguard.java` + `ConjureCardToHandEffect` |
| attack-triggered named card conjured into your library with perpetual abilities | `s/SanguineSoothsayer.java` + `ConjureCardIntoControllerLibraryEffect` |
| activated spellbook choice that conjures one named card into hand | `c/ChargedConjuration.java` + `ConjureCardFromSpellbookToHandEffect` |
| attack-triggered draft of three random spellbook cards into hand | `r/RecruitInstructor.java` + `DraftCardFromSpellbookToHandEffect` |
| batched combat-damage trigger that randomly conjures a spellbook card into playable exile | `d/DazzlingFlameweaver.java` + `ConjureRandomCardFromSpellbookToExileMayPlayUntilNextTurnEffect` |
| ally creature enters if it was cast; sacrifice it and conjure a perpetually modified duplicate | `p/PrototypeX8.java` | `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD TriggeringPermanentConditionalEffect(PermanentCastBySourceControllerThisTurnPredicate, SacrificeTriggeringPermanentThenConjureDuplicateEffect(...))` |
| perpetually gain selected keywords of another creature that enters under your control | `m/MutablePupa.java` | `ON_ALLY_CREATURE_ENTERS_BATTLEFIELD PerpetuallyGainKeywordsOfTriggeringCreatureEffect()` |
| landfall perpetually grants a random library land a tap-draw trigger | `a/AmbassadorOfEvendo.java` | `ON_ALLY_LAND_ENTERS_BATTLEFIELD PerpetuallyGrantTapDrawToRandomLandInLibraryEffect()` |
| random opponent gains control of source permanent | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| conjure a named card into the top N cards of a library with a perpetual casting option | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| choose an opponent, then you and that player each create tokens | `ChooseOpponentEachCreatesTokensEffect` |
| upkeep creates tokens for each opponent meeting a hand-size threshold | `CreateTokenEffect(new PlayersWithCardsInHandAtLeast(CountScope.OPPONENTS, threshold), ...)` |

- Lich (2ED 114): `LoseLifeEqualToLifeTotalAsEntersEffect` on `ON_ENTER_BATTLEFIELD`; static `CantLoseGameFromLifeEffect` and `NefariousLichLifeGainReplacementEffect`; `SacrificePermanentsOrLoseGameEffect(EventValue, not-token)` on `ON_CONTROLLER_DEALT_DAMAGE`; `ControllerLosesGameEffect` on `ON_DEATH`. Entry life loss is a replacement, damage sacrifice is triggered, and only a battlefield-to-graveyard departure triggers the explicit loss.

Purpose: quickly find a reference card for the pattern you're implementing. One or two examples per archetype. All paths relative to `cards/`.

| as-enters number choice, noncreature spells with chosen mana value can't be cast | `TalionTheKindlyLord` + `NoncreatureSpellsWithChosenManaValueCantBeCastEffect` |

This index has been split into smaller files for faster lookup. Each file is under 10k tokens.

## File index

| File | Sections | When to use |
|------|----------|-------------|
| [CARD_PATTERNS_LANDS_SPELLS.md](CARD_PATTERNS_LANDS_SPELLS.md) | Lands, Spells | Implementing lands (basic, pain, check, fast, creature, utility) or spells (burn, pump, destroy, board wipe, draw, mill, counterspell, modal, graveyard, steal, extra turn) |
| [CARD_PATTERNS_CREATURES_ETB.md](CARD_PATTERNS_CREATURES_ETB.md) | Vanilla, Keyword, ETB creatures | Implementing creatures with no abilities, keyword-only creatures, or ETB triggers |
| [CARD_PATTERNS_CREATURES_TRIGGERED.md](CARD_PATTERNS_CREATURES_TRIGGERED.md) | Triggered creatures | Implementing creatures with triggered abilities (attack, block, death, damage, upkeep, draw, spell cast, graveyard) |
| [CARD_PATTERNS_PERMANENTS_STATIC.md](CARD_PATTERNS_PERMANENTS_STATIC.md) | Static permanents, Auras | Implementing lords/anthems, static restrictions, auras (lockdown, boost, curse) |
| [CARD_PATTERNS_PERMANENTS_ARTIFACTS.md](CARD_PATTERNS_PERMANENTS_ARTIFACTS.md) | Artifacts, Vehicles, Equipment | Implementing artifacts, vehicles, or equipment |
| [CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md](CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md) | Activated abilities, Planeswalkers, Sagas | Implementing activated abilities (tap, sacrifice, mana, pump), planeswalkers, or sagas |
| [CARD_COPY_PASTE_TEMPLATES.md](CARD_COPY_PASTE_TEMPLATES.md) | Full card + test templates | Ready-to-use copy-paste templates for the most common archetypes (pump, burn, draw, destroy, ETB, counter, aura, aura with ability). Use line offsets from its quick selector table. |

## Quick pattern-to-file lookup

| Pattern keyword | File |
|----------------|------|
| d20, roll a d20, graveyard target ETB | CARD_PATTERNS_CREATURES_ETB.md |
| whammy deck, reveal until Island, choose to stop | EFFECTS_QUICK_REFERENCE.md and EFFECTS_INDEX.md |
| land, basic, pain, check, fast, manland | CARD_PATTERNS_LANDS_SPELLS.md |
| burn, damage, shock, bolt, X burn | CARD_PATTERNS_LANDS_SPELLS.md |
| pump, boost, giant growth, overrun | CARD_PATTERNS_LANDS_SPELLS.md |
| destroy, terror, wrath, board wipe, total power and toughness target restriction | CARD_PATTERNS_LANDS_SPELLS.md |
| each player sacrifices artifacts, enchantments, and nonbasic lands, then searches for basics | WaveOfVitriolEffect |
| draw, mill, discard, tutor, search | CARD_PATTERNS_LANDS_SPELLS.md |
| spellbook, draft from a spellbook, digital card offer | `DraftCardFromSpellbookEffect` + shared `LibraryRevealChoice` + `PerpetuallyMakeSelectedSpellbookCardArtifactCreatureEffect` (`y/SupportSkyforge.java`, YDFT 26) |
| seek a card and discard that exact card later | `SeekLibraryToHandAndRegisterDiscardAtNextEndStepEffect` + `DiscardSpecificCardEffect` |
| opponent searches library, control search choices, exile found cards | CARD_PATTERNS_PERMANENTS_STATIC.md |
| look at top cards, plot from library | CARD_PATTERNS_LANDS_SPELLS.md |
| exile top cards, play this turn, unplayed exiled cards to graveyard and tokens | `g/GlimpseTheImpossible.java` |
| double any effect that doubles, quadruple | EFFECTS_QUICK_REFERENCE.md and ORACLE_TEXT_EFFECT_MAP.md |
| counter, counterspell, cancel | CARD_PATTERNS_LANDS_SPELLS.md |
| remove any number of counters from among permanents | CARD_PATTERNS_LANDS_SPELLS.md |
| bounce, unsummon, return to hand | CARD_PATTERNS_LANDS_SPELLS.md |
| choose a creature type, return all other creatures to hand | `ReturnAllCreaturesExceptChosenTypeToHandEffect` + resolution-time creature-type choice; `r/RaiseThePalisade.java` |
| graveyard return, reanimate, flashback | CARD_PATTERNS_LANDS_SPELLS.md |
| target player's graveyard to bottom in random order | CARD_PATTERNS_CREATURES_ETB.md |
| modal, choose one, fight, bite | CARD_PATTERNS_LANDS_SPELLS.md |
| Case, solve, solved | CARD_PATTERNS_PERMANENTS_STATIC.md |
| steal, threaten, extra turn | CARD_PATTERNS_LANDS_SPELLS.md |
| directional multiplayer creature-choice control spell | CARD_PATTERNS_LANDS_SPELLS.md and EFFECTS_QUICK_REFERENCE.md |
| auction, bid life | CARD_PATTERNS_LANDS_SPELLS.md |
| tempting offer, each opponent may accept an effect | EFFECTS_QUICK_REFERENCE.md and ORACLE_TEXT_EFFECT_MAP.md |
| vanilla, no abilities, empty body | CARD_PATTERNS_CREATURES_ETB.md |
| keyword creature, flying, haste, infect | CARD_PATTERNS_CREATURES_ETB.md |
| lose a keyword and prevent opponents from gaining it | CARD_PATTERNS_PERMANENTS_STATIC.md and EFFECTS_QUICK_REFERENCE.md |
| first matching spell cast each turn costs less | CARD_PATTERNS_PERMANENTS_STATIC.md |
| random greatest-mana-value creature card in hand perpetually costs less | `f/FuelTankFeaster.java` + `PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect` |
| creature card in your graveyard without unearth perpetually gains unearth; max speed makes first unearth free | `h/HighwayReaver.java` + `PerpetuallyGrantUnearthToTargetCreatureCardEffect` + `MaxSpeedFreeFirstUnearthEffect` |
| enchantment spell and Room unlock cost reduction | CARD_PATTERNS_PERMANENTS_STATIC.md |
| ETB, enters the battlefield | CARD_PATTERNS_CREATURES_ETB.md |
| ETB secretly choose an opponent permanent, then opponent sacrifices another and the chosen permanent | `TargetPlayerChoosesCreatureOrPlaneswalkerThenSacrificesChosenPermanentEffect` + CARD_PATTERNS_CREATURES_ETB.md |
| airbend, exile target nonland permanent for a {2} cast | CARD_PATTERNS_CREATURES_ETB.md |
| airbend all other creatures, opponents can't cast from outside hand | CARD_PATTERNS_LANDS_SPELLS.md |
| kicker, alternate casting cost | CARD_PATTERNS_CREATURES_ETB.md |
| Squad, repeatable additional cost, token copies, attacking tokens | CARD_PATTERNS_PERMANENTS_STATIC.md |
| buyback, return spell to hand | CARD_PATTERNS_LANDS_SPELLS.md |
| attack trigger, death trigger, upkeep trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| attack trigger, memory counter, copy exiled creature cards | CARD_PATTERNS_CREATURES_TRIGGERED.md and EFFECTS_QUICK_REFERENCE.md |
| controller end-step trigger, memory counter, copy creature card in exile | `t/TheAnimus.java` and EFFECTS_QUICK_REFERENCE.md |
| combat damage → untap creatures + additional combat + repeat-player attack restriction | `p/PortRazer.java` |
| combat damage modal, goad damaged player's creature, exile top card and cast with any-color mana | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| +1/+1 counter placement trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| counters placed on a creature you don't control | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| beginning-of-combat random counter trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| beginning-of-combat random-opponent attack trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| face-down permanent turns face up | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| suspect a creature / clear suspected creatures | `SuspectEffect(GrantScope.TARGET)` + `UnsuspectAllCreaturesEffect`; for optional non-targeted selection use `MayEffect(SuspectChosenOtherCreatureEffect())` |
| combat damage trigger, block trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| combat damage to an opponent, same damage to each other opponent | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| graveyard trigger, graveyard ability | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| spell cast trigger, opponent spell | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| cast trigger reveals each player's top card and sets entry counters | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| first spell each turn, random opponent damage | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| beginning-of-combat random opponent attack requirement | `r/RuhanOfTheFomori.java` |
| attack-triggered left/right pile evasion | `r/RagingRiver.java` + `RagingRiverEffectHandler` |
| global spell-cast exile/copy trigger | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| first instant, sorcery, or subtype spell each turn — exile the triggering spell, dig to a nonland, damage by mana-value difference, and offer a free cast | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| combat damage → random own-graveyard instant/sorcery, free cast at next upkeep | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| chosen creature type, copy each matching creature you control, temporary hasty copies | `CreateTokenCopyOfEachCreatureOfChosenTypeEffect` + `CreateTokenCopyOfTargetPermanentEffect(true, true)` |
| chosen creature type, reveal until matching creature count, put matches onto battlefield | `RevealUntilChosenCreatureTypeCountToBattlefieldEffect` |
| encore, graveyard self-exile and attacking token copies for each opponent | `ExileSelfFromGraveyardCost` + `CreateTokenCopiesOfSourceAttackingOpponentsEffect` in a sorcery-speed graveyard ability |
| destroy target creature, then create two half-sized token copies | `DestroyTargetCreatureAndCreateTokenCopiesEffect` |
| cast-time X doubling, copy X spells or abilities | CARD_PATTERNS_PERMANENTS_STATIC.md |
| hand exile + token copy | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| encore, graveyard ability creates hasty copies attacking each opponent | `i/ImpulsivePilferer.java` + `EncoreEffect` |
| landfall, land enters trigger | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| each opponent may investigate, opponent choice plus controller Clues | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| lord, anthem, static boost | CARD_PATTERNS_PERMANENTS_STATIC.md |
| commander-only anthem, opponent attacks with two creatures | CARD_PATTERNS_PERMANENTS_STATIC.md and CARD_PATTERNS_CREATURES_TRIGGERED.md |
| damage prevention into counters | CARD_PATTERNS_PERMANENTS_STATIC.md |
| aura, enchant creature, pacifism | CARD_PATTERNS_PERMANENTS_STATIC.md |
| curse, enchant player | CARD_PATTERNS_PERMANENTS_STATIC.md |
| metalcraft, morbid, conditional | CARD_PATTERNS_PERMANENTS_STATIC.md |
| quest counter, opponent end step trigger, life-loss condition | CARD_PATTERNS_PERMANENTS_STATIC.md |
| pay-life trigger, counters from life paid, counter-removal ability | CARD_PATTERNS_PERMANENTS_STATIC.md |
| protection from modified creatures | CARD_PATTERNS_PERMANENTS_STATIC.md |
| as-enters card-type choice, controller and own creatures gain protection from chosen card type | `ChooseCardTypeOnEnterEffect` + `GrantProtectionFromChosenCardTypeToControllerAndOwnCreaturesEffect` |
| postcombat main may-pay-life draw based on opponents dealt combat damage | CARD_PATTERNS_CREATURES_TRIGGERED.md |
| artifact, charge counter, spellbomb | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| create a token this turn, conditional draw artifact ability | `i/IdolOfOblivion.java` |
| vehicle, crew | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| equipment, equip, living weapon | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| activated ability, tap ability, sacrifice ability | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| X-paid face-down creature cast from hand with damage/tap turn-up clause | CARD_PATTERNS_PERMANENTS_ARTIFACTS.md |
| base power from target creature, indefinitely | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| mana ability, mana dork | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| commander-identity mana plus shared-creature-type spell trigger | CARD_PATTERNS_LANDS_SPELLS.md |
| planeswalker, loyalty | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| saga, chapter, lore counter | CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md |
| template, copy-paste, skeleton | CARD_COPY_PASTE_TEMPLATES.md |

## Canonical test reference per pattern

When implementing a card, use these as the **best** test file to read for each common pattern:

| Pattern | Best test reference | Why |
|---------|-------------------|-----|
| Activated ability requiring mana from a multicolored-capable source | `e/ExperimentFive.java` / `ExperimentFiveTest.java` | `PayMulticoloredSourceManaCost` consumes source-capability-tagged mana before the ability's ordinary generic mana cost |
| Aura with static boost (+X/+Y or -X/-Y) | `SensoryDeprivationTest.java` | Covers casting, resolution, stat check, removal, fizzle, targeting |
| Aura lockdown (can't attack/block) | `PacifismTest.java` | Covers combat restriction + removal |
| Targeted temporary static combat tax | `WhipgrassEntanglerTest.java` | Covers dynamic attack/block payment and cleanup duration |
| Simple burn spell | `ShockTest.java` | Covers creature + player targeting + fizzle |
| Non-targeted pump | `ChargeTest.java` | Covers boost + opponent unaffected + cleanup reset |
| ETB creature (non-targeted) | `AngelOfMercyTest.java` | Covers ETB trigger resolution |
| ETB creature (targeted) | `BriarpackAlphaTest.java` | Covers targeted ETB + fizzle + flash |
| Counterspell | `CancelTest.java` | Covers counter + graveyard |
| Target creature exile with suspend counters | `s/SuspendTest.java` | Covers exile, suspend countdown, free cast, and creature-only targeting |
| Draw spell | `CounselOfTheSoratamiTest.java` | Covers draw count + graveyard |
| Destroy spell | `TerrorTest.java` | Covers destroy + filter + fizzle |
| Equipment | `LeoninScimitarTest.java` | Covers equip + boost + unequip |
| Lord/anthem | `GloriousAnthemTest.java` | Covers static boost + removal |
| Vanilla creature | (no test needed) | Empty body, no engine logic |
| Tapped artifact token with its own targeted ETB and produced-mana trigger | `r/RoxanneStarfallSavant.java` | Use the full `CreateTokenEffect` constructor for non-creature token state, put the damage ability in `ON_ENTER_BATTLEFIELD`, and put the dynamic mana rider in `ON_SELF_TAPPED_FOR_MANA` |
| ETB creates an Equipment token and attaches it to the source | `u/USAgentJohnWalker.java` | Use `CreateTokenAndAttachToSourceEffect(CreateTokenEffect.ofArtifactToken(...).withTokenEffects(Map.of(STATIC, new StaticBoostEffect(...))))`; put the token's `EquipActivatedAbility` in its token ability list |

### Subgames

Shahrazad (ARN 10): `StartSubgameEffect` followed by the existing fractional life-loss effect with `SUBGAME_NON_WINNERS`. The creating spell stays suspended until its immediate child ends. Use `GameSession` for nesting; never copy the parent board into a child or restore an old library snapshot.
