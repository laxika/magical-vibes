# Subjects reference

Who or what an effect acts on. A verb effect takes a `PermanentSubject`, `PlayerSubject` or `CardSubject` built with the factory
`Subjects` (domain, `model/effect/`); `SubjectResolutionService` (engine, `service/effect/`) is the only code that
resolves one. "Targets" = the variant declares the effect's target. "Static" = a static ability may use it (the
resolver's static entry point rejects the others).

## PermanentSubject

| Factory | Card wording | Targets | Static |
|---|---|---|---|
| `source()` | "this creature", "this permanent" (the entry's source permanent only, never its target) | no | yes |
| `target()`, `target(group)` | "target creature", "target permanent" | yes | no |
| `allTargets()` | "untap up to four target permanents" whose targets the ability's target filter or several slots declare (every permanent target of the entry, whichever group it was chosen for) | yes | no |
| `attached()` | "enchanted permanent", "enchanted land" (now while the source is on the battlefield, else as it last was) | no | yes |
| `attachedCreature()` | "enchanted creature", "equipped creature": `attached()` that a resolving effect names only while the host is a creature; a static ability judges the host without a type read (an Aura or Equipment on a noncreature it can't be attached to leaves it through state-based actions), and the engine's attachment-restriction query and the AI tell it from `attached()` | no | yes |
| `attachedOrSource()` | "this creature ... If it's an Aura, enchanted creature ... instead" (a bestow card's own ability: the host while the source, as it is or last was, is attached, else the source itself) | no | yes |
| `attachedAtTrigger()` | "that creature" after "at the beginning of the upkeep of enchanted creature's controller" (the host the stack entry recorded when the ability triggered or was activated, not the current one; Paroxysm, Venarian Gold; "that creature" after "whenever enchanted creature deals damage", Guilty Conscience) | no | no |
| `triggering()` | "it", "that creature" (the permanent the trigger event was about) | no | no |
| `triggeringAll()` | "them", "for each of them" (every permanent of a "one or more" event, else the one the event was about; Kambal, Profiteering Mayor). A one-permanent verb's `triggering()` may read it for "one of them" (token copies, tokens attached to a creature) | no | no |
| `triggeringOrSource()` | "it" after "whenever this creature or equipped creature attacks" (a reconfigure card: the permanent the event recorded, else the source, whose own event records none) | no | no |
| `triggeringCard()` | "that card" of a "whenever you play a card" event (the spell or land; with `ownerOf`, "its owner", which for an event with no spell or land is the owner of the card put into a graveyard or of the permanent that entered) | no | no |
| `targetSpell()` | "target spell", only inside `controllerOf` ("that spell's controller"): read from the stack, else as it was countered (a counter-unless-pays rider has no target, only that record); names no permanent; the spell target is declared by the effect reading it (damage) or by the counter effect it rides on (discard) | no | no |
| `targetAbilitySource()`, `targetAbilitySource(filter)` | "that permanent" after "counter target activated or triggered ability" ("destroy that permanent", "that permanent's activated abilities can't be activated this turn", "that permanent loses all abilities"): the permanent the targeted ability came from, read from the stack while the ability is there, else as the counter step recorded it (`StackEntry.counteredAbilitySourceId`); nothing for a spell target, a sourceless ability or a permanent that left; `filter` judges it as the reading effect applies (Tishana's Tidebinder). The target is declared by the counter effect beside it (Teferi's Response, Ouphe Vandals, Interdict, Trickbind) | no | no |
| `triggeringSpell()` | "that spell or ability", only inside `controllerOf`: the spell or ability the trigger event was about, as it is on the stack or last was ("that spell or ability's controller sacrifices a land", Lava Runner) | no | no |
| `returned()` | "that creature" after "return target card ... to the battlefield" (step result) | no | no |
| `returnedThisResolution()` | "exile them again" after an earlier step of the same resolution returned permanents (every permanent the entry recorded as returned; step result; Lae'zel's Acrobatics) | no | no |
| `exiledThisResolution()` | "return that card" after an earlier step of the same resolution exiled the effect's target: names no permanent on the battlefield; `permanentIds` gives the target's id, whose card the entry remembered as it left (step result; Gilraen, Dúnedain Protector) | no | no |
| `granter()` | "return this Equipment to its owner's hand" in an ability the Equipment grants (the granting permanent: bound at activation as `granter(id)`, else the granted trigger's recorded granter) | no | no |
| `named(permanentId)` | "that creature" in a delayed triggered ability a resolving effect created about another permanent than its trigger event ("when this leaves the battlefield or becomes untapped, remove all -1/-1 counters from the creature", Giant Oyster: bound by `RemoveAllCountersEffect.aimedAt`; a permanent that left and came back is not named) | no | no |
| `controlledBy(players, filter)` | "each creature you control", "all creatures", "each land target player controls" (the filter knows the source, X, the defending player, the triggering player and permanent: "tap all lands that player controls") | only if `players` targets | if `players` may |
| `ownedBy(players, filter)` | "all artifacts target player owns" (each matching permanent whose card's owner is one of `players`, whoever controls it; the effect declares a player target) | no | if `players` may |
| `attachmentsOf(permanents, filter)` | "all Auras attached to target permanent" (each matching permanent attached to one of the named permanents still on the battlefield; the attachments are not targeted) | no | if `permanents` may |
| `allOf(subjects...)` | "this creature and target creature" | if one part targets | if every part may |
| `othersItsControllerControlsSharing(permanent, quality, filter)` | "all other creatures that player controls with the same name as that creature", "all tokens that player controls with the same name as that permanent": as `otherPermanentsSharing`, among the named permanent's controller's only; `filter` describes just those others (Legion's End, Legions to Ashes) | no | no |
| `triggeringAttackers()` | "they gain ...", "those creatures" in a "whenever you attack" ability: the attackers the entry recorded as the ability triggered, those still on the battlefield (Valor's Reach, Angelic Guardian) | no | no |
| `triggeringSpellTargets()` | "those creatures" naming the targets of the spell a trigger was about (Storm Windrider): that spell's targets as it is on the stack, else as the entry recorded them; with no triggering spell, the effect's own targets, which the card's target filter declares (Myojin of Towering Might) | no | no |
| `createdThisResolution()` | "those tokens" after an earlier step of the same resolution created them (every permanent the entry recorded as created, still on the battlefield; step result; Gilt-Leaf Ambush, Tend to the Kiln); in a delayed ability `RegisterDelayedStepTriggerEffect.aboutCreated` made, the ones that resolution created | no | no |
| `thisWay(StepKind)` | "those creatures", "if a creature is dealt damage this way, it ..." (Provoke the Trolls): the permanents the most recent step of that kind recorded on the entry's scratchpad, still on the battlefield as the same objects; only `PUT_COUNTERS`, `REMOVE_COUNTERS`, `DEAL_DAMAGE`, `MANIFEST` ("put a +1/+1 counter on it" after a manifest, Formless Nurturing; tokens: `createdThisResolution()`, returned permanents: `returnedThisResolution()`; other kinds are refused). `thisWay(kind, filter)` names only those matching `filter` as the reading effect is applied ("if a Dragon is dealt damage this way, destroy it", The Black Arrow; "a creature dealt damage this way can't block", Ballista Wielder). Step result | no | no |
| `bandmatesOf(permanent)` | "each other creature banded with it" (CR 702.22): the other attacking creatures in the band of the permanent `permanent` names, `source()` as it is or last was; nothing outside a band (Icatian Skirmishers) | no | no |
| `withCreaturesSharingAColor(permanent)` | "target creature and each other creature that shares a color with it": the named creature and every creature sharing a color with it as the effect resolves; nothing once the named permanent is gone or no longer a creature (Surge of Zeal; Rally the Righteous untaps them; Wojek Siren boosts them) | if `permanent` targets | no |
| `otherCreaturesSharing(permanent, quality)`, `otherPermanentsSharing(permanent, quality, filter)` | "each other creature that shares a color with it", "each other creature with the same name as that creature", "each other creature that shares a creature type with it", "all other permanents with the same name as that permanent", "each other nonland permanent with the same mana value as that permanent" (`SharedQuality.COLOR` / `NAME` / `CREATURE_TYPE` / `MANA_VALUE`): every permanent matching `filter` (creatures for `otherCreaturesSharing`) but the one `permanent` names sharing that with it as the effect resolves; nothing once the named permanent is gone or no longer matches `filter`. A face-down permanent has no name; one with all card names shares every name. It only reads `permanent`; the target is declared by the target's own effect beside it or by an `allOf(target(), …)` (Cleansing Beam, Homing Lightning, Killer, Echoing Courage; Maelstrom Pulse, Leave No Trace, Echoing Truth, Mists of Lórien) | no | no |

Named sets, one shared instance each (a static ability's set is judged per permanent, so a card should use these rather
than build its own): `creaturesYouControl()` / `otherCreaturesYouControl()`, `otherTappedCreaturesYouControl()`,
`otherUntappedCreaturesYouControl()`, `creaturesYourOpponentsControl()`, `permanentsYouControl()` /
`otherPermanentsYouControl()`, `creaturesTargetPlayerControls()` / `landsTargetPlayerControls()` (declare the player
target), `allCreatures()` / `otherCreatures()`, `allPermanents()` / `otherPermanents()`,
`creaturesEnchantedPlayerControls()`, `landsYouControl()`, `landsYourOpponentsControl()`, `allLands()` / `otherLands()`,
`sourceAndPaired()` (soulbond, CR 702.95: the source and its pair, as a set). Pick by the card's words: "creatures you
control" includes the source, "other creatures you control" does not; a one-shot "all creatures gain ..." is
`allCreatures()`.
| `except(permanents, excluded)` | "then exile all other Nightmare tokens you control" after the copy step: `except(controlledBy(you(), Nightmare token), createdThisResolution())` (the permanents `permanents` names but those `excluded` names; neither part may target; Abyssal Harvester) | no | if both may |
| `oneOf(options...)` | "exile this permanent or up to one target creature": one of the options, chosen by the controller as the effect resolves; the resolver refuses it, the verb's handler resolves each option and asks; a target option is declared by the ability (Kaya, Ghost Assassin) | no | no |

## PlayerSubject

| Factory | Card wording | Targets | Static |
|---|---|---|---|
| `you()` | "you" | no | yes |
| `targetPlayer()`, `targetPlayer(group)` | "target player", "target opponent" | yes | no |
| `targetPlayerDeclaredElsewhere()` | "target player" whose target the card's target filter or another effect of the ability declares ("target player draws two cards and loses 2 life"); reads the same targets | no | no |
| `eachPlayer()`, `eachOpponent()` | "each player", "each opponent" (APNAP order) | no | yes |
| `opponentsOf(player)` | "each of that player's opponents", "each other player" (APNAP order; nobody if `player` names nobody) | no | if `player` may |
| `controllerOf(permanent)` | "its controller", "that creature's controller" (last-known once it left) | no | if `permanent` may |
| `ownerOf(permanent)` | "its owner" | no | if `permanent` may |
| `triggeringPlayer()` | "that player" (the player the trigger event was about) | no | no |
| `activePlayer()` | "the active player" | no | yes |
| `defendingPlayer()` | "defending player" | no | no |
| `enchantedPlayer()` | "enchanted player" (a Curse's player) | no | yes |
| `chosenPlayer()` | "the chosen player" (chosen as the source entered; while the enters ability that chooses it is still on the stack, that ability's target: Laquatus's Champion; for a spell, the player an earlier effect of the spell chose onto the entry: Backdraft) | no | yes |
| `chosenPlayers()` | "the chosen players" (every player chosen as the source entered) | no | yes |
| `acceptedPlayers()` | "each opponent who discarded their hand this way": the players who said yes to the preceding "each player may" choice of the same resolution (step result); `except(acceptedPlayers(), you())` for the opponents among them | no | no |
| `playersDealtDamageThisResolution()` | "each player dealt damage this way": the players an earlier step of the same resolution dealt damage to, as the entry recorded them (step result; Chandra, Roaring Flame's emblem) | no | no |
| `playersThisWay(StepKind)` | "that player" after "choose an opponent at random" (`CHOOSE`, `ChoosePlayerAtRandomEffect`), after "return target permanent to its owner's hand" (Recoil), "its controller" after an exile or destroy step (Swords to Plowshares), "each player who drew / milled / lost life this way": the players the most recent step of that kind recorded (whom it happened to, or the controllers, owners for cards and returns, of what it acted on as it acted, CR 608.2h). Not `DEAL_DAMAGE` (`playersDealtDamageThisResolution()`), `COUNTER_SPELL` (`controllerOf(targetSpell())`) or `COPY_SPELL`. Step result | no | no |
| `subgameWinners()` | "each player who doesn't win that game" is `except(eachPlayer(), subgameWinners())`: the winners of the subgame an earlier step of the same resolution played (step result; Shahrazad; resolving it with no recorded result is an engine error) | no | no |
| `except(players, excluded)` | "the other chosen player": `except(chosenPlayers(), triggeringPlayer())` | no | if both may |
| `allOf(players...)` | "that player or that planeswalker's controller": `allOf(targetPlayerDeclaredElsewhere(), controllerOf(target()))` over a "target player or planeswalker" another effect declares (each player once, APNAP order) | no | if every part may |
| `matching(players, PlayerPredicate)` | "each player who controls the most lands": the players `players` names who match the filter as the effect is applied (`matching(eachPlayer(), new PlayerControlsMostLandsPredicate())`, Tectonic Hellion; the filter is a `PlayerPredicate`, evaluated with the effect's controller as the comparing player) | no | if `players` may |

## CardSubject

Cards outside the battlefield. The resolver names each card as it is now, in the public zone it is expected to be in: a
card a step of the same resolution moved to a public zone is found there (CR 400.7j), and a card that has left that
zone since is a new object (CR 400.7) and is not named. The exceptions are a sought card (digital only), which is
followed into the hand the seek put it in, and a card chosen from a revealed hand, named while it is still there. `SubjectResolutionService.cards` / `locatedCards` (the card,
its zone and the player whose zone it is). No variant is static.

| Factory | Card wording | Targets |
|---|---|---|
| `cardsThisWay(StepKind)` | "create a token that's a copy of it" after "exile target card" (`EXILE`: the exiled cards, in exile), "return each card put into a graveyard this way" (`DESTROY` / `SACRIFICE`: the card each removed permanent became in the zone it went to, from the step's `StepOutcome.movedCards`; a token becomes none), "the discarded card" (`DISCARD`: in its graveyard, or in exile where a replacement put it), "exile it instead … you may cast it" after a counter (`COUNTER_SPELL`: the countered spell's card in its graveyard or exile; a copy, an ability or a card put into a hidden zone becomes none), "that card perpetually gains menace" (`SEEK`: the sought cards in the hand), "from among the cards milled this way" (`MILL`: in a graveyard, or in exile where a replacement put them, CR 701.17c), "the chosen card", "it" after a choice (`CHOOSE`: the card `ChooseCardEffect` or a revealed-hand choice that keeps it picked, in the zone and stay it was chosen in). Other kinds are refused. `cardsThisWay(kind, filter)` names only the moved cards matching `filter` as they are now ("that card" after "exile cards from the top of your library until you exile a nonland card": `cardsThisWay(EXILE, ExileCardsFromTopUntilEffect.NONLAND)`, Territorial Bruntar, Kang Prime). Step result (The Scarab God, Sorin Lord of Innistrad, Malcolm, Silvan Reveler, Spelljack) In a delayed ability `RegisterDelayedStepTriggerEffect.aboutExiled` made it names that resolution's cards, each only in the stay it had then (CR 603.7c); a merged permanent is each of its cards (CR 731.3c). | no |
| `targetCard(filter, scope)` | "a copy of target creature card in your graveyard": the effect's targeted card while it is in a graveyard of `scope` and matches `filter`; the effect reading it declares the graveyard target (Feldon of the Third Path) | yes |
| `triggeringCards(filter)` | "one of them" after "whenever you discard one or more nonland cards": the event's cards matching `filter`, each in the graveyard stay the event put it in (Conspiracy Theorist) | no |

Readers: `CreateTokenCopyOfCardEffect.of(cards, options)`, `ChooseCardEffect` ("choose one of them"), `BecomeCopyOfSubjectEffect.of(cards)` ("becomes a copy of that card"), `ExileCardsWithSameNameEffect.anyNumber/all/every(cards, ...)` ("the same name as that card"),
`BecomeForetoldEffect(cards, reducedBy)`, `DiscardSpecificCardEffect.of(cards)`, `ExileSpecificCardFromHandEffect(cards)`, the perpetual grants
(`PerpetuallyGrantCardCharacteristicsEffect.to(cards)`, `PerpetuallyGrantKeywordsToTriggeringCardEffect.toCards(cards, ...)`,
`PerpetuallyBecomeOnlyCardTypeEffect(cards, type)`), `PutCounterOnExiledCardsEffect(cards, counter)` ("with a memory counter on it"), `PutCardsIntoHandEffect(cards)` ("if you don't, put that card into your hand"), `ReturnAllCardsExiledWithSourceEffect.putOntoBattlefield(cards)` ("put that card onto the battlefield"), `ReturnCardFromGraveyardEffect.builder().returnAll(true).cards(...)`,
`MayCastDiscardedCardWithoutPayingManaCostEffect(cards[, filter])` ("you may cast it without paying its mana cost": a card in exile whoever owns
it, Transcendent Dragon; a graveyard card only from your own graveyard), `AllowPlayCardsFromExileEffect` ("you may
play that card this turn / until the end of your next turn / for as long as it remains exiled"),
`CopyImprintedCardAndMayCastCopyEffect.of(cards)` ("copy it. You may cast the copy"), `ExileGraveyardCardsEffect.cards(cards)` ("exile it
from your graveyard") and, in a filter, `CardSharesCardTypeWithCardsPredicate(cards)` ("a card that doesn't share a card type with the
discarded card"; bound by `SubjectResolutionService.bindCardSubjects`).

A `target()` inside `controllerOf` / `ownerOf` reads the entry's target without declaring one ("destroy target
creature; its controller loses 2 life"). A `PermanentIsTargetCardPredicate` leaf in a `controlledBy` filter is the entry's target permanent as the
effect resolves ("each other creature that player controls").

A resolving effect that changes no characteristics makes a rule of a set, not a locked set (CR 611.2c: "creatures
target player controls can't block this turn" covers creatures arriving later): `SubjectResolutionService.lastingRule`
binds the set's players as the effect resolves (`eachPlayer()` binds none) and a `PermanentIsTargetCardPredicate` leaf to
the effect's target permanent, and returns a predicate judged later without a stack entry. A rule kept per player
(can't attack, "don't untap during that player's next untap step") registers on each player the subject names and judges
the set's filter when the rule is checked. A static ability whose source is off the battlefield (an emblem's) resolves a
set with `permanents(GameData, source, controllerId, subject)`.

`Subjects.narrowed(set, filter)` narrows a set subject (`controlledBy`, `ownedBy`, `attachmentsOf`) by an effect's own
filter.

A static ability's handler asks about one permanent at a time: `SubjectResolutionService.names(StaticEffectContext,
subject, filter, StaticReads)` (through `StaticEffectSupport.names`) says whether the subject names the context's
target. It judges `source()`, `attached()`, `attachedOrSource()`, `named()`, `allOf()` and `controlledBy()` (with
`you()`, `eachPlayer()`, `eachOpponent()`, `enchantedPlayer()` read off the context, other players through the list
form) without looking anything up; creature, land and the effect's filter are answered by the static pass's
recursion-safe reads. `ownedBy` and `attachmentsOf` have no one-permanent form yet.

The same check is asked outside the static handlers. `names(GameData, source, sourceControllerId, targetControllerId,
target, subject, StaticReads)` takes whose battlefield the target is on, or is entering, instead of searching for it
(a creature still entering, an enchanted player's creatures), and accepts a `null` source for an ability with no source
permanent (an emblem's: `source()` and `attached()` then name nothing). The layered pass answers creature and land from
the states of the board it is building (`LayerSystemService.scopeTargets`; an effect's own filter is judged and
memoized apart, per filter instance, CR 613.6); a continuous query outside the pass asks
`GameQueryService.staticSubjectNames`, which answers them with the layered queries; a caller that already judged the
permanent's type passes `StaticReads.knownCreature()` or `knownLand()` (that part of a set holds, any other does not).

A static restriction on players asks about one player: `SubjectResolutionService.namesPlayer(source,
controllerId, playerId, players)` judges `you()`, `eachPlayer()`, `eachOpponent()`, `enchantedPlayer()` and
`chosenPlayer()` without a lookup (a `null` source enchants and chose nobody).
