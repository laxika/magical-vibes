#!/usr/bin/env python3
"""Step 15, run 17z: the agent-docs lines that name the cast family's removed records and markers or the changed card
forms.

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
record outside a "Gone" line.

Usage: step15-run17z-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17z"
COUNTERS = "`PutCountersOnSourceEffect(1, 1, 1)`"
CHOSEN = "`CardHasSourceChosenColorPredicate()`"
CASUALTY = ("`addTrigger(Triggers.youCastThisSpell(), TriggerOptions.DEFAULT.withInterveningIf(new CasualtyCostPaid()), "
            "new CopyTriggeringSpellEffect())`")
JACE_EMBLEM = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.opponentCastsSpell().nthThisTurn(1), "
               "new CounterSpellEffect()), \"…\")`")
GARRUK_EMBLEM = ("`new CreateEmblemEffect(new TriggeredAbility(Triggers.youCastSpell().withFilter(new "
                 "CardTypePredicate(CardType.CREATURE)), new MayEffect(new SearchLibraryEffect(creature, "
                 "LibrarySearchDestination.BATTLEFIELD), prompt)), \"…\")`")
BLOODBOND = ("`Triggers.playerCastsSpell().withFilter(new CardTypePredicate(CardType.CREATURE))` + "
             "`EachPlayerReturnsCardsFromGraveyardToBattlefieldEffect(Integer.MAX_VALUE, new "
             "CardSharesNameWithTriggeringSpellPredicate())`")
DONT_WORRY = ("`Triggers.youCastSpell().withStackEntryFilter(new StackEntryIsCardEnchantedBySourcePredicate())` + "
              "`CopyTriggeringSpellEffect()`")
TABLET = ("`Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate())` + "
          "`GainLifeEffect(new TriggeringSpellChosenColorCount())`")
PRISM = "`Triggers.youCastSpell().withFilter(new CardHasSourceChosenColorPredicate())` + `GainLifeEffect(1)`"
POISON = ("`Triggers.youCastSpell().withFilter(new CardAllOfPredicate(List.of(new CardTypePredicate(CREATURE), "
          "new CardKeywordPredicate(INFECT))))` + `GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER)`")

# (file, line start, new line, "replace" | "after")
EDITS = [
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "Enchant card in your hand. Enchanted card costs {1} less to cast. When you cast enchanted card, copy it." |',
     '| "Enchant card in your hand. Enchanted card costs {1} less to cast. When you cast enchanted card, copy it." | '
     '`target(HandCardPredicateTargetFilter(CardTruePredicate))` + `SPELL EnchantCardInHandEffect()` + `STATIC '
     f'ReduceEnchantedHandCardCastCostEffect()` + {DONT_WORRY} | SPELL + STATIC + filtered cast trigger | Don\'t Worry '
     'About It (MB2 353); the hand-card attachment survives while the card remains in hand; the copy is made from the '
     'spell as the ability resolves, or as it last existed on the stack |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "Whenever you cast a legendary spell from your hand, exile cards from the top of your library until you exile a legendary nonland card with lesser mana value.',
     '| "Whenever you cast a legendary spell from your hand, exile cards from the top of your library until you exile '
     'a legendary nonland card with lesser mana value. You may cast that card without paying its mana cost. Put the '
     'rest on the bottom of your library in a random order." | `CascadeEffect(false, new CardSupertypePredicate('
     'LEGENDARY), false, true)` | `Triggers.youCastSpell().withFilter(new CardSupertypePredicate(LEGENDARY))'
     '.withStackEntryFilter(new StackEntryCastFromZonePredicate(Zone.HAND))` | Jodah, the Unifier (DMU). The cascade '
     'reads the triggering spell\'s mana value as it resolves |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "You get an emblem with \\"Whenever you cast a creature spell, you may search your library',
     '| "You get an emblem with \\"Whenever you cast a creature spell, you may search your library for a creature card, '
     f'put it onto the battlefield, then shuffle.\\"" | {GARRUK_EMBLEM} | loyalty ability | Garruk, Caller of Beasts '
     '−7. The emblem\'s cast ability is read by the generic cast collector from the command zone; it resolves before '
     'the creature spell, the "you may" asked as it resolves |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "You get an emblem with \\"Whenever an opponent casts their first spell each turn, counter that spell.\\"" |',
     '| "You get an emblem with \\"Whenever an opponent casts their first spell each turn, counter that spell.\\"" | '
     f'{JACE_EMBLEM} | loyalty ability | Jace, Unraveler of Secrets −8. `.nthThisTurn(1)` counts the caster\'s spells '
     'this turn, so it triggers on any turn, once per opponent per turn, and a spell that can\'t be countered still '
     'uses up the first spell. Erayo\'s Essence writes the same trigger and effect on the permanent |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "Whenever a player casts a creature spell, each player returns all cards with the same name as that spell',
     '| "Whenever a player casts a creature spell, each player returns all cards with the same name as that spell from '
     f'their graveyard to the battlefield" | {BLOODBOND} | filtered cast trigger | Bloodbond March (RAV). The name is '
     'read from the triggering spell as the ability resolves (the spell, else as it last existed on the stack); every '
     'player returns the matching cards from their own graveyard |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "Whenever a player casts a spell, put a +1/+1 counter on this creature" |',
     f'| "Whenever a player casts a spell, put a +1/+1 counter on this creature" | {COUNTERS} | '
     '`Triggers.playerCastsSpell().anySpell()` | Managorger Hydra; "whenever an opponent casts a spell" is '
     '`Triggers.opponentCastsSpell().anySpell()` (Ishai, Thundering Mightmare\'s granted ability), "a green spell" '
     '`.withFilter(new CardColorPredicate(CardColor.GREEN))` (Titania\'s Chosen), "a spell that\'s white, blue, black, '
     'or red" a `CardAnyOfPredicate` of `CardColorPredicate`s (Quirion Dryad) |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "As this creature enters, choose a color. Whenever you cast a spell of the chosen color, put a +1/+1 counter on this creature" |',
     '| "As this creature enters, choose a color. Whenever you cast a spell of the chosen color, put a +1/+1 counter on '
     f'this creature" | `STATIC ChooseColorOnEnterEffect()` + {COUNTERS} | `Triggers.youCastSpell().withFilter({CHOSEN[1:-1]})` '
     '| Diamond Knight. The filter reads the spell\'s colors as effects leave them, so a multicolored spell containing '
     'the color triggers; colorless spells do not |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "As this artifact enters, choose two colors. Whenever you cast a spell, if it\'s at least one of the chosen colors,',
     '| "As this artifact enters, choose two colors. Whenever you cast a spell, if it\'s at least one of the chosen '
     f'colors, you gain 1 life for each of the chosen colors it is." | `STATIC ChooseColorOnEnterEffect(2)` + {TABLET} '
     '| triggered ability | Tablet of the Guilds; the filter judges "at least one of the chosen colors" as it '
     'triggers and the count reads the spell\'s colors and the source\'s choice as it resolves (no life for a spell '
     'that is none of them any more) |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "As this artifact enters, choose a color. Whenever you cast a spell of the chosen color, you gain 1 life." |',
     '| "As this artifact enters, choose a color. Whenever you cast a spell of the chosen color, you gain 1 life." | '
     f'`STATIC ChooseColorOnEnterEffect()` + {PRISM} | triggered ability | Prism Ring, Diamond Mare; the color is a '
     'trigger condition (CR 603.2), so a spell that changes color in response still gains the life |', "replace"),
    ("agent-docs/ORACLE_TEXT_EFFECT_MAP.md", '| "At the beginning of your end step, choose a random nonland Magic card name.',
     '| "At the beginning of your end step, choose a random nonland Magic card name. Until your next end step, you may '
     'cast a copy of a spell with that name, and mana of any type can be spent to cast it. If you cast a spell this '
     'way, copy this ability if Discord is on the battlefield." | `Triggers.endStep(ActivePlayer.YOU) '
     'DiscordRandomCardCopyEffect()` | Discord, Lord of Disharmony (SLD 798). The handler chooses from the implemented '
     'card catalog by distinct nonland name, creates the copy in exile with an end-step expiry and any-mana permission, '
     'and links it to Discord; casting the linked copy puts the copied ability on the stack while Discord is on the '
     'battlefield (`SpellCastingService`), with no cast trigger |', "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `EnchantCardInHandEffect()` + `ReduceEnchantedHandCardCastCostEffect()` +",
     "- `EnchantCardInHandEffect()` + `ReduceEnchantedHandCardCastCostEffect()` + "
     f"{DONT_WORRY} - the hand-attached Aura path for Don't Worry About It (MB2 353): the target remains a hand card, "
     "its controller's cast cost is reduced by one generic, and casting it creates a copy that may choose new targets.",
     "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `CasualtyCost.matchingChosenX()` requires",
     "- `CasualtyCost.matchingChosenX()` requires the sacrificed creature's effective power to be at least the spell's "
     "chosen X (CR 702.153a). Ob Nixilis, the Adversary's copy is `new CopyTriggeringSpellEffect(true)"
     ".withoutSupertypes(Set.of(CardSupertype.LEGENDARY)).withStartingLoyaltyFromX()` behind the casualty \"if\".",
     "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `CasualtyCost(int minimumPower)` —",
     None, "fragment:payment marks the spell for `CopyThisSpellIfCasualtyPaidEffect`.=>payment marks the spell's casualty payment, which `CasualtyCostPaid` reads."),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `CopyThisSpellIfCasualtyPaidEffect()` —",
     f"- Casualty's copy — {CASUALTY}: \"when you cast this spell, if a casualty cost was paid for it, copy it\" "
     "(CR 702.153a); the intervening \"if\" is judged as it triggers and again from the trigger's entry, which records "
     f"the spell's payment, as it resolves. `CopyThisSpellIfCasualtyPaidEffect` is **Gone** ({RUN}).", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `TriggeringSpellChosenColorCount(",
     "- `TriggeringSpellChosenColorCount()` — effective colors of the triggering spell that are among the colors the "
     "ability's source chose, both read as it resolves (the source as it last existed once it has left the "
     "battlefield); use as a `DynamicAmount` for \"for each of the chosen colors it is\" (Tablet of the Guilds).",
     "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect(int amount)` —",
     f"- \"Whenever you cast a spell of the chosen color, put a +1/+1 counter on this creature\" — "
     f"`Triggers.youCastSpell().withFilter({CHOSEN[1:-1]})` + {COUNTERS} (Diamond Knight); pair with "
     "`ChooseColorOnEnterEffect()`. `PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect` and "
     f"`PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` are **Gone** ({RUN}): a color filter "
     "(`CardColorPredicate`) or `.anySpell()` and the same effect", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect()` —",
     f"- \"Whenever a player casts a creature spell, each player returns all cards with the same name as that spell from "
     f"their graveyard to the battlefield\" — {BLOODBOND} (Bloodbond March); the name is read as the ability resolves. "
     f"`ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect` is **Gone** ({RUN})", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `GivePoisonCountersEffect(int, TARGET_PLAYER, CardPredicate spellFilter)` —",
     f"- \"Whenever you cast a creature spell with infect, target player gets a poison counter\" — {POISON} (Hand of "
     f"the Praetors); the effect's `spellFilter` component is gone ({RUN})", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "Tablet of the Guilds uses `ChooseColorOnEnterEffect(2)`",
     "Tablet of the Guilds uses `ChooseColorOnEnterEffect(2)` to store two distinct colors on the entering permanent, "
     f"then {TABLET}: the filter is \"at least one of the chosen colors\" and the count gains one life for each selected "
     "color the spell still is as the ability resolves.", "replace"),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "`DiscordRandomCardCopyEffect()` + `DiscordCopyCastTriggerEffect()` —",
     "`DiscordRandomCardCopyEffect()` — chooses a random distinct nonland name from the implemented card catalog, "
     "creates its copy in exile with permission through the controller's next end step and any-mana casting, and links "
     "it to Discord; casting that copy puts the copied ability on the stack while Discord remains on the battlefield "
     f"(Discord, Lord of Disharmony, SLD 798). `DiscordCopyCastTriggerEffect` is **Gone** ({RUN}).", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `CopyThisSpellIfCasualtyPaidEffect` |",
     f"| `CopyThisSpellIfCasualtyPaidEffect` | — | **Gone** ({RUN}): casualty's copy is {CASUALTY} |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "- `TriggeringSpellChosenColorCount(Set<CardColor> chosenColors)` — the number of effective colors of",
     "- `TriggeringSpellChosenColorCount()` — the number of effective colors of", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "  the spell that caused the current triggered ability that are among `chosenColors`. Snapshot the",
     "  the spell that caused the current triggered ability that are among the colors the ability's source chose, both", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "  chosen colors when the trigger goes on the stack; the spell's colors are read on resolution",
     "  read as it resolves (the source as it last existed once it has left the battlefield)", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect` |",
     f"| `ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect` | — | **Gone** ({RUN}): Bloodbond "
     f"March writes {BLOODBOND} |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` |",
     f"| `PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect` | — | **Gone** ({RUN}): \"whenever [a player / you / "
     f"an opponent] casts a [color] spell, put a +1/+1 counter on this creature\" is the cast trigger's color filter "
     f"(`CardColorPredicate`, a `CardAnyOfPredicate` of them, or `.anySpell()`) and {COUNTERS} (Managorger Hydra, "
     "Titania's Chosen, Quirion Dryad, Ishai, Thundering Mightmare) |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect` |",
     f"| `PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect` | — | **Gone** ({RUN}): Diamond Knight writes "
     f"`Triggers.youCastSpell().withFilter({CHOSEN[1:-1]})` + {COUNTERS} |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `CounterOpponentFirstSpellEachTurnEffect` |",
     f"| `CounterOpponentFirstSpellEachTurnEffect` | — | **Gone** ({RUN}): \"whenever an opponent casts their first "
     "spell each turn, counter that spell\" is `Triggers.opponentCastsSpell().nthThisTurn(1)` + `CounterSpellEffect()` "
     f"(Erayo's Essence; Jace, Unraveler of Secrets' emblem: {JACE_EMBLEM}) |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `GainLifeForEachChosenColorSpellCastEffect` |",
     f"| `GainLifeForEachChosenColorSpellCastEffect` | — | **Gone** ({RUN}): Tablet of the Guilds writes {TABLET}; Prism "
     f"Ring and Diamond Mare {PRISM} |", "replace"),
    ("agent-docs/EFFECTS_INDEX.md", "| `GivePoisonCountersEffect` | `(int amount, PoisonRecipient recipient)`, `(DynamicAmount, recipient)` or `(int amount, PoisonRecipient recipient, CardPredicate spellFilter)` |",
     None, "fragment:`(int amount, PoisonRecipient recipient)`, `(DynamicAmount, recipient)` or `(int amount, PoisonRecipient recipient, CardPredicate spellFilter)`=>`(int amount, PoisonRecipient recipient)` or `(DynamicAmount, recipient)`"),
    ("agent-docs/EFFECTS_INDEX.md", "| `GivePoisonCountersEffect` |",
     None, "fragment:TARGET_PLAYER (targets a player; with `spellFilter` doubles as an `Triggers.youCastSpell()` trigger descriptor firing when the controller casts a matching spell, resolving into a `spellFilter == null` copy — Hand of the Praetors)=>TARGET_PLAYER (targets a player; Hand of the Praetors' filtered cast trigger)"),
    ("agent-docs/EFFECTS_INDEX.md", "| `CopyTriggeringSpellEffect` | `([boolean tokenCopy[, boolean retargetToSource]])` + `with…` modifiers |",
     None, "fragment:`withoutSupertypes(Set)` (\"except the copy isn't legendary\"), =>`withoutSupertypes(Set)` (\"except the copy isn't legendary\"), `withStartingLoyaltyFromX()` (\"it has loyalty X\": Ob Nixilis, the Adversary's casualty copy), "),
    ("agent-docs/EFFECTS_QUICK_REFERENCE.md", "- `CopyTriggeringSpellEffect([boolean tokenCopy[, boolean retargetToSource]])` —",
     None, "fragment:`withoutSupertypes(Set)`, `asAdditionally=>`withoutSupertypes(Set)`, `withStartingLoyaltyFromX()`, `asAdditionally"),
    ("agent-docs/EFFECTS_INDEX.md", "`Kicked`, `RepeatedAdditionalCostPaid(manaCost)`",
     None, "fragment:`Kicked`, `RepeatedAdditionalCostPaid(manaCost)` (optionally `minimumPayments` to distinguish repeated matching payments), =>`Kicked`, `RepeatedAdditionalCostPaid(manaCost)` (optionally `minimumPayments` to distinguish repeated matching payments), `CasualtyCostPaid` (casualty's intervening \"if\"), "),
    ("agent-docs/PREDICATES_REFERENCE.md", "| `StackEntryIsCardExiledWithSourcePredicate` |",
     "| `StackEntryIsCardEnchantedBySourcePredicate` | `()` | the spell whose card the source Aura enchants in a hand: "
     "\"when you cast enchanted card\" (Don't Worry About It's `withStackEntryFilter`) |", "after"),
    ("agent-docs/PREDICATES_REFERENCE.md", "| `CardHasSourceChosenColorPredicate` |",
     "| `CardHasSourceChosenColorPredicate` | `()` | a card containing a color chosen by the source permanent (any of "
     "them when it chose several: Tablet of the Guilds' \"at least one of the chosen colors\"); needs the `GameData` "
     "overload and the source card ID, and is useful for spell-cast triggers such as Jeweled Torque, Prism Ring and "
     "Diamond Knight |", "replace"),
    ("agent-docs/CARD_PATTERNS_PERMANENTS_STATIC.md", "| Keyword lord + spell trigger | `h/HandOfThePraetors.java` |",
     "| Keyword lord + spell trigger | `h/HandOfThePraetors.java` | STATIC StaticBoostEffect with "
     "PermanentHasKeywordPredicate(INFECT) filter, OWN_CREATURES scope + Triggers.youCastSpell().withFilter("
     "CardAllOfPredicate(CREATURE, INFECT)) GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PLAYER) |", "replace"),
    ("agent-docs/CARD_PATTERNS_PERMANENTS_ARTIFACTS.md", "| Choose two colors + per-color spell-cast life | `t/TabletOfTheGuilds.java` |",
     "| Choose two colors + per-color spell-cast life | `t/TabletOfTheGuilds.java` | STATIC ChooseColorOnEnterEffect(2) "
     "+ Triggers.youCastSpell().withFilter(CardHasSourceChosenColorPredicate) GainLifeEffect(TriggeringSpellChosenColorCount) "
     "— store two distinct colors, then gain one life for each chosen color the spell is |", "replace"),
    ("agent-docs/CARD_PATTERNS_PERMANENTS_ARTIFACTS.md", "| Choose one color + spell-cast life | `p/PrismRing.java` |",
     "| Choose one color + spell-cast life | `p/PrismRing.java` | STATIC ChooseColorOnEnterEffect() + "
     "Triggers.youCastSpell().withFilter(CardHasSourceChosenColorPredicate) GainLifeEffect(1) — gain 1 life per spell "
     "of the chosen color |", "replace"),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md", "| Any spell +1/+1 counter | `m/ManagorgerHydra.java` |",
     "| Any spell +1/+1 counter | `m/ManagorgerHydra.java` | Triggers.playerCastsSpell().anySpell() "
     "PutCountersOnSourceEffect(1, 1, 1) — any spell, colorless included |", "replace"),
    ("agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md", "| Spell cast trigger | `q/QuirionDryad.java` |",
     "| Spell cast trigger | `q/QuirionDryad.java` | Triggers.youCastSpell().withFilter(CardAnyOfPredicate of "
     "CardColorPredicates) PutCountersOnSourceEffect(1, 1, 1) |", "replace"),
    ("agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md", "| Planeswalker with reveal-creatures dig + green creature from hand + creature-tutor emblem |",
     None, "fragment:−7 `CreateEmblemEffect` (marker `SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect.Marker`; each creature cast queues `MayEffect(SearchLibraryEffect(creature, BATTLEFIELD))`)=>−7 " + GARRUK_EMBLEM + " (the generic cast collector reads the emblem's cast ability)"),
    ("agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md", "| Planeswalker with scry+draw + bounce + first-spell-counter emblem |",
     None, "fragment:-8 `CreateEmblemEffect(List.of(new CounterOpponentFirstSpellEachTurnEffect.Marker()), \"…\")`; TriggerCollectionService queues CounterSpellEffect (Zone.STACK) when an opponent's spellsCastThisTurnCount == 1=>-8 " + JACE_EMBLEM),
    ("agent-docs/LAYER_SYSTEM.md", "`PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect`,", None, "delete"),
]

GONE = ["PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect",
        "PutPlusOnePlusOneCounterOnSourceOnChosenColorSpellCastEffect", "GainLifeForEachChosenColorSpellCastEffect",
        "ReturnSameNameCardsFromGraveyardsToBattlefieldOnCreatureSpellCastEffect", "CopyEnchantedHandCardOnCastEffect",
        "CounterOpponentFirstSpellEachTurnEffect", "SearchCreatureToBattlefieldOnControllerCastsCreatureSpellEffect",
        "DiscordCopyCastTriggerEffect", "JodahTheUnifierTriggerEffect", "CopyThisSpellIfCasualtyPaidEffect"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "after":
        if new in lines:
            return False
        if not idx:
            raise SystemExit(f"anchor not found: {start}")
        lines.insert(idx[0] + 1, new)
        return True
    if mode == "delete":
        if not idx:
            return False
        del lines[idx[0]]
        return True
    if mode.startswith("fragment:"):
        old, repl = mode[len("fragment:"):].split("=>", 1)
        if any(repl in line for line in lines) and not any(old in lines[i] for i in idx):
            return False
        for i in idx:
            if repl in lines[i]:
                return False
            if old in lines[i]:
                lines[i] = lines[i].replace(old, repl)
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
