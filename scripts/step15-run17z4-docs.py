#!/usr/bin/env python3
"""Step 15, run 17z4: the agent-docs lines that name the removed played-name and land-tap trigger-condition records
(`PlayedCardExiledWithSourceDrawAndTransformTriggerEffect`, `PlayedCardNameMatchesCardExiledWithSourceTriggerEffect`,
`PlayedCardExiledWithSourceTriggerEffect`, `PlayedCardExiledWithPlanarSourceTriggerEffect`, `DealDamageOnLandTapEffect`,
`GainLifeWhenOpponentTapsLandOfSubtypeEffect`, `OpponentTappedLandDoesntUntapEffect`), and the new authoring forms:
`SpellTrigger.exiledWithThis()` / `LandPlayTrigger.exiledWithThis()`, `CardSharesNameWithCardExiledWithSourcePredicate`,
`PutCardExiledWithSourceIntoHandEffect.withTriggeringCardNameToOwnersHand()`, `LoseLifeRecipient.TRIGGERING_CARD_OWNER`,
`ExileTopCardOfTriggeringPlayerLibraryEffect.ofTriggeringCardOwner()`, `LibraryScope.TRIGGERING_PLAYER`,
`Triggers.opponentTapsLandForMana()`, the land-tap trigger's `withFilter` and
`DealDamageToPlayersEffect.dealtByTriggeringPermanent()`.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
record outside a "Gone" line.

Usage: step15-run17z4-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17z4"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
CI = "agent-docs/CARD_PATTERN_INDEX.md"
CP = "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md"
PR = "agent-docs/PREDICATES_REFERENCE.md"
AG = "agent-docs/ACTIVATED_ABILITY_GUIDE.md"
PC = "agent-docs/PLANECHASE.md"

DAMAGE = "`DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER)`"
SEARCH = ("`MayEffect(PutCardExiledWithSourceIntoHandEffect.withTriggeringCardNameToOwnersHand(), prompt)` + "
          "`ConditionalEffect(new NoCardsExiledWithSource(), new ConditionalEffect(new SourceIsOnBattlefield(), "
          "SequenceEffect.of(new SacrificeSelfEffect(), new ControllerExtraTurnEffect(1))))` on "
          "`Triggers.youCastSpell().withFilter(new CardSharesNameWithCardExiledWithSourcePredicate())` and "
          "`Triggers.youPlayLand().withFilter(new CardSharesNameWithCardExiledWithSourcePredicate())`")
EXILED = ("a cast ability on `Triggers.<caster>CastSpell().exiledWithThis()` and a land-play ability on "
          "`Triggers.<player>PlayLand().exiledWithThis()` (\"a card exiled with this\": played from exile while exiled "
          "with the ability's source, judged from the event by the cast and land-play collectors)")
MATRIX = ("`SequenceEffect.of(new LoseLifeEffect(3, LoseLifeRecipient.TRIGGERING_CARD_OWNER), "
          "ExileTopCardOfTriggeringPlayerLibraryEffect.ofTriggeringCardOwner())` on `Triggers.youCastSpell()"
          ".exiledWithThis()` and `Triggers.youPlayLand().exiledWithThis()`")
BARBFLARE = ("`SequenceEffect.of(new AddOneOfEachManaTypeProducedByLandEffect(false), new DealDamageToPlayersEffect(1, "
             "DamageRecipient.TRIGGERING_PLAYER).dealtByTriggeringPermanent())`")

EDITS = [
    (OM, "| \"whenever an opponent taps a [Subtype] for mana, you may gain N life\" |",
     "| \"whenever an opponent taps a [Subtype] for mana, you may gain N life\" | `new MayEffect(new GainLifeEffect(N), "
     "prompt)` | `Triggers.opponentTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(subtype))` | Sanctimony "
     "(Mountain, 1). The land-tap collector judges the tapping player and the land filter as the land is tapped and "
     "puts the ability on the stack as written; the \"you may\" is asked as it resolves (CR 603.5) |", "line"),
    (OM, "| \"When this enchantment enters, exile the top five cards of your library. Whenever you play a card with "
     "the same name as one of the exiled cards,",
     "| \"When this enchantment enters, exile the top five cards of your library. Whenever you play a card with the "
     "same name as one of the exiled cards, you may put one of those cards with that name into its owner's hand. Then "
     "if there are no cards exiled with this enchantment, sacrifice it. If you do, take an extra turn after this "
     "one.\" | `ExileTopCardsToSourceEffect(5, false)` (Triggers.enters(ObjectRelation.THIS)) + " + SEARCH + " | see "
     "effect column | Search the City. \"Play a card\" = cast a spell **or** play a land (its ruling), hence the two "
     "abilities; \"that name\" is the stack entry's triggering spell's, or the land played's, read as the ability "
     "resolves |", "line"),
    (OM, "| \"{T}: This creature deals 2 damage to you. Exile the top card of your library. You may play that card "
     "this turn. Activate only as a sorcery. When you play a card exiled with this creature,",
     "| \"{T}: This creature deals 2 damage to you. Exile the top card of your library. You may play that card this "
     "turn. Activate only as a sorcery. When you play a card exiled with this creature, transform this creature.\" | "
     "`ActivatedAbility(..., List.of(DealDamageToPlayersEffect(2, CONTROLLER), "
     "ExileTopCardToSourceAndMayPlayThisTurnEffect()))` + `TransformSelfEffect()` on `Triggers.youCastSpell()"
     ".exiledWithThis()` and `Triggers.youPlayLand().exiledWithThis()` | activated ability + triggered abilities | "
     "Voltaic Visionary. The exiled card is source-tracked so the trigger fires only when that specific card is "
     "played; land plays and spell casts from exile are both covered. |", "line"),
    (OM, "| \"Whenever Prowl attacks, exile up to one other target tapped creature or Vehicle.",
     "| \"Whenever Prowl attacks, exile up to one other target tapped creature or Vehicle. For as long as that card "
     "remains exiled, its owner may play it. Whenever a player plays a card exiled with Prowl, you draw a card and "
     "convert Prowl.\" | `ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffect()` on Triggers.attacks() + "
     "`DrawCardEffect(1)`, `TransformToBackFaceEffect()` on `Triggers.playerCastsSpell().exiledWithThis()`, "
     "`Triggers.youPlayLand().exiledWithThis()` and `Triggers.opponentPlaysLand().exiledWithThis()` | triggered "
     "abilities | Prowl, Stoic Strategist; the source-tracked permission covers both spell casts and land plays from "
     "exile. |", "line"),
    (OM, "| \"Whenever a [permanent] an opponent controls becomes tapped, you may gain N life\" |", "",
     "fragment:Contrast Sanctimony's `GainLifeWhenOpponentTapsLandOfSubtypeEffect` (mana taps only)=>Contrast "
     "Sanctimony's `Triggers.opponentTapsLandForMana().withFilter(...)` (mana taps only)"),
    (OM, "| \"that land doesn't untap during its controller's next untap step\" (snow land tap) |",
     "| \"that land doesn't untap during its controller's next untap step\" (snow land tap) | "
     "`TappedSnowLandDoesntUntapEffect()` | Triggers.playerTapsLandForMana() | Winter's Night. Symmetric; the "
     "opponent-only, any-land ability that uses the stack is `SkipNextUntapEffect(TapUntapScope.TRIGGERING)` on "
     "`Triggers.opponentTapsLandForMana()` (Vorinclex, Voice of Hunger) |", "line"),
    (OM, "| \"whenever an opponent taps a land for mana, that land doesn't untap",
     "| \"whenever an opponent taps a land for mana, that land doesn't untap during its controller's next untap "
     "step\" | `new SkipNextUntapEffect(TapUntapScope.TRIGGERING)` | `Triggers.opponentTapsLandForMana()` | Vorinclex, "
     "Voice of Hunger. Uses the stack; the tapped land is the entry's triggering permanent |",
     "after-anchor:| \"that land doesn't untap during its controller's next untap step\" (snow land tap) |"),
    (OM, "| \"whenever a player taps a land for mana, if this creature is tapped, that player adds one mana of any "
     "type that land produced. Then that land deals 1 damage to that player\" |",
     "| \"whenever a player taps a land for mana, if this creature is tapped, that player adds one mana of any type "
     "that land produced. Then that land deals 1 damage to that player\" | " + BARBFLARE + " with "
     "`TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped())` | Triggers.playerTapsLandForMana() | Barbflare "
     "Gremlin (DSC 26). The trigger collector gates the source-tapped condition and resolves the mana-and-damage "
     "sequence immediately as one triggered mana ability; the tapped land, as it last existed if its mana ability "
     "made it leave, deals the damage |", "line"),
    (OM, "| \"whenever a player taps a land for mana, [this] deals 1 damage to that player\"",
     "| \"whenever a player taps a [land / Island / nonbasic land] for mana, [this] deals 1 damage to that player\" | "
     + DAMAGE + " | `Triggers.playerTapsLandForMana()`, `.withFilter(land predicate)` | Manabarbs, Tember City; Scald "
     "(Island), Burning Earth (nonbasic). Uses the stack; the tapping player is the entry's non-targeting \"that "
     "player\" and triggering player |",
     "after-anchor:| \"whenever an opponent taps a [Subtype] for mana, you may gain N life\" |"),
    (OM, "| \"When you planeswalk to The Matrix of Time,",
     "| \"When you planeswalk to The Matrix of Time, each player exiles the top card of their library. During your "
     "turn, you may play lands and cast spells from among cards exiled with The Matrix of Time. Whenever you play a "
     "land or cast a spell from among cards exiled with The Matrix of Time, that card's owner loses 3 life and exiles "
     "the top card of their library. Whenever chaos ensues, create two Treasure tokens.\" | "
     "`ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect()` + `AllowPlayCardsExiledWithPlanarSourceEffect()` + "
     + MATRIX + " + `CreateTokenEffect.ofTreasureToken(2)` | planar arrival / static / spell-cast and land-play "
     "triggers / chaos trigger | The Matrix of Time (WHO 589) |", "line"),
    (QR, "- `PlayedCardExiledWithSourceTriggerEffect()` / `(CardEffect followUpEffect)`",
     "- `PlayedCardExiledWithSourceTriggerEffect` — **Gone** (" + RUN + "): \"when you play a card exiled with this\" "
     "is " + EXILED + " with the follow-up effect itself (Voltaic Visionary's `TransformSelfEffect()`, Share the "
     "Spoils' `ExileTopCardsToSourceEffect(1, false, false, LibraryScope.TRIGGERING_PLAYER)`)", "line"),
    (QR, "- `PlayedCardExiledWithSourceDrawAndTransformTriggerEffect()`",
     "- `PlayedCardExiledWithSourceDrawAndTransformTriggerEffect` — **Gone** (" + RUN + "): Prowl, Stoic Strategist "
     "writes `DrawCardEffect(1)` and `TransformToBackFaceEffect()` on " + EXILED, "line"),
    (QR, "- `ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect()`",
     "- `ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect()` — on planar arrival, exile and track the top card "
     "of each player's library with the resolving `PlanarObject`; pair with "
     "`AllowPlayCardsExiledWithPlanarSourceEffect()` and abilities on `Triggers.youCastSpell().exiledWithThis()` / "
     "`Triggers.youPlayLand().exiledWithThis()` (the plane is the source the card was exiled with) for Matrix of "
     "Time-style permissions and triggers.", "line"),
    (QR, "- `ExileTopCardOfTriggeringPlayerLibraryEffect()`",
     "- `ExileTopCardOfTriggeringPlayerLibraryEffect()` — exile the top card of the player stored in the resolving "
     "trigger entry's `targetId`. As an `Triggers.dealsCombatDamageToPlayer()` trigger it binds the damaged player "
     "(ingest, Fathom Feeder). `ExileTopCardOfTriggeringPlayerLibraryEffect.ofTriggeringCardOwner()`: the owner of the "
     "card a \"whenever you play a card\" event was about (The Matrix of Time).", "line"),
    (QR, "- `PlayedCardExiledWithPlanarSourceTriggerEffect(CardEffect followUpEffect)`",
     "- `PlayedCardExiledWithPlanarSourceTriggerEffect` — **Gone** (" + RUN + "): The Matrix of Time writes " + MATRIX,
     "line"),
    (QR, "- `PutCardExiledWithSourceIntoHandEffect([CardPredicate filter])`",
     "- `PutCardExiledWithSourceIntoHandEffect([CardPredicate filter[, boolean toOwnersHand]])` — put one matching "
     "card the controller owns exiled with the source into hand (chooses if several); wrap in `MayEffect`. Endless "
     "Horizons upkeep, Gustha's Scepter `{T}` ability, Ethereal Forager's attack trigger. `returningToOwner()` returns "
     "the chosen linked card to its owner, including after source control changes. "
     "`withTriggeringCardNameToOwnersHand()`: \"one of those cards with that name\", the name of the card the trigger "
     "event was about (the triggering spell, or the land played), read as the ability resolves (Search the City).",
     "line"),
    (QR, "`DealDamageToPlayersEffect.dealtByEventDamageSource()` —",
     "`DealDamageToPlayersEffect.dealtByTriggeringPermanent()` — the permanent the trigger event was about (the "
     "entry's triggering permanent, as it is or as it last existed, CR 608.2h) deals the damage: \"then that land "
     "deals 1 damage to that player\" (Barbflare Gremlin).", "after"),
    (IX, "| `DealDamageOnLandTapEffect` |",
     "| `DealDamageOnLandTapEffect` | — | **Gone** (" + RUN + "): \"whenever a player taps a [filter] land for mana, "
     "this deals N damage to that player\" is " + DAMAGE + " on `Triggers.playerTapsLandForMana()` with the land "
     "filter as its `withFilter` (Manabarbs, Tember City, Scald, Burning Earth); Overabundance and Barbflare Gremlin "
     "write the damage as the second step of their triggered mana ability, Barbflare's `.dealtByTriggeringPermanent()` "
     "|", "line"),
    (IX, "| `GainLifeWhenOpponentTapsLandOfSubtypeEffect` |",
     "| `GainLifeWhenOpponentTapsLandOfSubtypeEffect` | — | **Gone** (" + RUN + "): Sanctimony writes "
     "`Triggers.opponentTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(MOUNTAIN))` + "
     "`MayEffect(new GainLifeEffect(1), prompt)` |", "line"),
    (IX, "| `PlayedCardExiledWithSourceTriggerEffect` |",
     "| `PlayedCardExiledWithSourceTriggerEffect` | — | **Gone** (" + RUN + "): " + EXILED + " with the follow-up "
     "effect itself (Voltaic Visionary, Share the Spoils) |", "line"),
    (IX, "| `PlayedCardExiledWithPlanarSourceTriggerEffect` |",
     "| `PlayedCardExiledWithPlanarSourceTriggerEffect` | — | **Gone** (" + RUN + "): The Matrix of Time writes " + MATRIX
     + " |", "line"),
    (IX, "| `PlayedCardExiledWithSourceDrawAndTransformTriggerEffect` |",
     "| `PlayedCardExiledWithSourceDrawAndTransformTriggerEffect` | — | **Gone** (" + RUN + "): Prowl, Stoic "
     "Strategist writes `DrawCardEffect(1)` and `TransformToBackFaceEffect()` on " + EXILED + " |", "line"),
    (IX, "| `PlayedCardNameMatchesCardExiledWithSourceTriggerEffect` |",
     "| `PlayedCardNameMatchesCardExiledWithSourceTriggerEffect` | — | **Gone** (" + RUN + "): Search the City writes "
     + SEARCH + " |", "after-anchor:| `PlayedCardExiledWithSourceDrawAndTransformTriggerEffect` |"),
    (IX, "| `PutCardExiledWithSourceIntoHandEffect` |",
     "| `PutCardExiledWithSourceIntoHandEffect` | `([CardPredicate filter[, boolean toOwnersHand]])`, "
     "`returningToOwner()`, `withTriggeringCardNameToOwnersHand()` | puts one matching card the controller owns exiled "
     "\"with\" the source permanent into their hand; with several the controller chooses which via a "
     "`LIBRARY_REVEAL_CHOICE` routed by `PendingReturnExiledWithSourceCard` (only the chosen card leaves exile). Wrap "
     "in `MayEffect` for \"you may\". Companion to `SearchLibraryForCardsToExileWithSourceEffect`. Used by Endless "
     "Horizons's Triggers.upkeep(ActivePlayer.YOU) and Ethereal Forager's filtered attack trigger. "
     "`withTriggeringCardNameToOwnersHand()` narrows the pool to the name of the card the trigger event was about (the "
     "stack entry's triggering spell, or the land played), read as the ability resolves, and returns it to its owner "
     "(Search the City) |", "line"),
    (IX, "| `OpponentTappedLandDoesntUntapEffect` |",
     "| `OpponentTappedLandDoesntUntapEffect` | — | **Gone** (" + RUN + "): Vorinclex, Voice of Hunger writes "
     "`Triggers.opponentTapsLandForMana()` + `SkipNextUntapEffect(TapUntapScope.TRIGGERING)`, the tapped land the "
     "entry's triggering permanent |", "line"),
    (IX, "| `TappedSnowLandDoesntUntapEffect` |",
     "| `TappedSnowLandDoesntUntapEffect` | `()` | Triggers.playerTapsLandForMana() trigger: whenever ANY player taps "
     "a **snow** land for mana, increments skipUntapCount on that land so it doesn't untap during its controller's "
     "next untap step, as Winter's Night's triggered mana ability resolves. The opponent-only stack ability is "
     "`SkipNextUntapEffect(TapUntapScope.TRIGGERING)` on `Triggers.opponentTapsLandForMana()` (Vorinclex). Winter's "
     "Night |", "line"),
    (IX, "| `DealDamageToPlayersEffect.dealtByEventDamageSource()` |",
     "| `DealDamageToPlayersEffect.dealtByTriggeringPermanent()` | `DamageSource.TRIGGERING_PERMANENT` | the permanent "
     "the trigger event was about (the entry's triggering permanent) deals the damage instead of the ability's source, "
     "as it is or as it last existed (CR 608.2h); no damage when the entry names none. \"Then that land deals 1 damage "
     "to that player\" (Barbflare Gremlin) (" + RUN + ") |", "after"),
    (IX, "| `LoseLifeRecipient.TRIGGERING_PERMANENT_CONTROLLER` |",
     "| `LoseLifeRecipient.TRIGGERING_CARD_OWNER` | enum value | `LoseLifeEffect` recipient: the owner of the card a "
     "\"whenever you play a card\" event was about (the stack entry's triggering spell's card, or the land played, "
     "`GameQueryService.findTriggeringCardOwner`). The Matrix of Time: \"that card's owner loses 3 life\" (" + RUN
     + ") |", "after"),
    (IX, "| `ExileTopCardsToSourceEffect` |", "",
     "fragment:leaves the battlefield (Duplicity) |=>leaves the battlefield (Duplicity). "
     "`LibraryScope.TRIGGERING_PLAYER` is the non-targeting \"their library\" of the player who played the card "
     "(Share the Spoils' \"when they do, exile the top card of their library\") |"),
    (PR, "| `StackEntryIsCardExiledWithSourcePredicate` |",
     "| `StackEntryIsCardExiledWithSourcePredicate` | `()` | spells whose exact card identity is currently exiled with "
     "the source permanent; on a cast trigger the cast collector reads it from the cast event (the card was cast from "
     "exile while exiled with the source permanent or the face-up plane), which `SpellTrigger.exiledWithThis()` "
     "writes with `castFrom(Zone.EXILE)`; its land-play counterpart is `LandPlayTrigger.exiledWithThis()` |", "line"),
    (PR, "| `CardHasSourceChosenNamePredicate` |",
     "| `CardSharesNameWithCardExiledWithSourcePredicate` | `()` | a card with the same name as one of the cards "
     "exiled with the source permanent, judged with the source (the `GameData` overload and the source card id); "
     "nothing without one. Search the City's cast and land-play filters (\"whenever you play a card with the same "
     "name as one of the exiled cards\") |", "after"),
    (AG, "| `Triggers.playerTapsLandForMana()` | Any player taps a land |",
     "| `Triggers.playerTapsLandForMana()` | Any player taps a land; `.withFilter(PermanentPredicate)` narrows the "
     "tapped land (Scald's Island, Burning Earth's nonbasic land), judged by the land-tap collector as it is tapped |",
     "line"),
    (AG, "| `Triggers.playerTapsLandForMana()` | Any player taps a land;",
     "| `Triggers.opponentTapsLandForMana()` | An opponent taps a land (Sanctimony, Vorinclex's untap lock); read by "
     "the `playerTapsLandForMana()` collector, which judges the tapping player |", "after"),
    (AG, "| `Triggers.youCastSpell()` | Controller casts a spell (\"whenever you cast...\") |",
     "| `Triggers.youCastSpell().exiledWithThis()`, `Triggers.youPlayLand().exiledWithThis()` | A card exiled with "
     "this permanent (or plane) is played from exile (Voltaic Visionary, Prowl, Share the Spoils, The Matrix of Time) "
     "|", "after"),
    (CI, "| planar arrival exiles and tracks each player's top card,",
     "| planar arrival exiles and tracks each player's top card, the controller may play those cards during their "
     "turn, and playing one makes its owner lose life and exiles another top card | `t/TheMatrixOfTime.java` + "
     "planar-source exile/play permission + `exiledWithThis()` cast and land-play abilities reading "
     "`LoseLifeRecipient.TRIGGERING_CARD_OWNER` |", "line"),
    (CP, "| Land tap trigger | `m/Manabarbs.java` |",
     "| Land tap trigger | `m/Manabarbs.java` | Triggers.playerTapsLandForMana() " + DAMAGE + " — the land-tap "
     "collector puts it on the stack as written with the tapping player as the entry's \"that player\" |", "line"),
    (CP, "| Land tap triggered mana ability with damage | `o/Overabundance.java` |",
     "| Land tap triggered mana ability with damage | `o/Overabundance.java` | Triggers.playerTapsLandForMana() "
     "`SequenceEffect.of(AddOneOfEachManaTypeProducedByLandEffect(false), " + DAMAGE.strip("`") + ")` — collected as "
     "one triggered mana ability: add the extra mana first, then deal the damage immediately without using the stack "
     "|", "line"),
    (CP, "| Tapped-source land tap triggered mana ability with damage | `b/BarbflareGremlin.java` |",
     "| Tapped-source land tap triggered mana ability with damage | `b/BarbflareGremlin.java` | "
     "Triggers.playerTapsLandForMana() `TriggerOptions.DEFAULT.withInterveningIf(new SourceIsTapped())` " + BARBFLARE
     + " — the source-tapped intervening \"if\" gates the same immediate mana-and-damage sequence; the tapped land "
     "deals the damage |", "line"),
    (CP, "| Nonbasic land tap trigger | `b/BurningEarth.java` |",
     "| Nonbasic land tap trigger | `b/BurningEarth.java` | Triggers.playerTapsLandForMana()"
     ".withFilter(PermanentNotPredicate(PermanentHasSupertypePredicate(BASIC))) " + DAMAGE + " — the filter narrows "
     "which tapped lands trigger it |", "line"),
    (CP, "| Land tap mana doubling + opponent untap lock | `v/VorinclexVoiceOfHunger.java` |",
     "| Land tap mana doubling + opponent untap lock | `v/VorinclexVoiceOfHunger.java` | "
     "Triggers.playerTapsLandForMana() AddOneOfEachManaTypeProducedByLandEffect + Triggers.opponentTapsLandForMana() "
     "`SkipNextUntapEffect(TapUntapScope.TRIGGERING)` — doubles controller's land mana, opponent lands skip next untap "
     "step |", "line"),
    (PC, "- `ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect` tracks each arrival-exiled card",
     "- `ExileTopCardOfEachPlayersLibraryWithPlanarSourceEffect` tracks each arrival-exiled card with the resolving "
     "planar object; a plane's `Triggers.youCastSpell().exiledWithThis()` / `Triggers.youPlayLand().exiledWithThis()` "
     "abilities trigger on a card played from that tracked exile (the cast and land-play collectors compare the "
     "event's exile source with the face-up plane) and keep the planar source on their stack entry.", "line"),
]

GONE = ["PlayedCardExiledWithSourceDrawAndTransformTriggerEffect", "PlayedCardNameMatchesCardExiledWithSourceTriggerEffect",
        "PlayedCardExiledWithSourceTriggerEffect", "PlayedCardExiledWithPlanarSourceTriggerEffect",
        "DealDamageOnLandTapEffect", "GainLifeWhenOpponentTapsLandOfSubtypeEffect",
        "OpponentTappedLandDoesntUntapEffect"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "after" or mode.startswith("after-anchor:"):
        if new in lines:
            return False
        anchor = mode[len("after-anchor:"):] if mode.startswith("after-anchor:") else start
        at = [i for i, line in enumerate(lines) if line.startswith(anchor)]
        if not at:
            raise SystemExit(f"anchor not found: {anchor}")
        lines.insert(at[0] + 1, new)
        return True
    if mode.startswith("fragment:"):
        old, repl = mode[len("fragment:"):].split("=>", 1)
        for i in idx:
            if repl in lines[i]:
                return False
            if old in lines[i]:
                lines[i] = lines[i].replace(old, repl, 1)
                return True
        raise SystemExit(f"fragment not found under {start}: {old}")
    if new in lines:
        return False
    if not idx:
        raise SystemExit(f"line not found: {start}")
    lines[idx[0]] = new
    return True


def main():
    by_file = {}
    for path, start, new, mode in EDITS:
        by_file.setdefault(path, []).append((start, new, mode))
    for path, edits in by_file.items():
        file = ROOT / path
        lines = file.read_text().split("\n")
        changed = False
        for start, new, mode in edits:
            changed |= apply(lines, start, new, mode)
        if changed:
            file.write_text("\n".join(lines))
            print(f"updated {path}")
    docs = list((ROOT / "agent-docs").glob("*.md")) + list((ROOT / ".claude/skills").rglob("SKILL.md")) \
        + list((ROOT / ".codex/skills").rglob("SKILL.md"))
    for doc in docs:
        for number, line in enumerate(doc.read_text().split("\n"), 1):
            for gone in GONE:
                if re.search(r"\b" + gone + r"\b", line) and "**Gone**" not in line:
                    raise SystemExit(f"{doc.relative_to(ROOT)}:{number} still names {gone}")


if __name__ == "__main__":
    main()
