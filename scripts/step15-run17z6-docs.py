#!/usr/bin/env python3
"""Step 15, run 17z6: the agent-docs lines that name the removed records (`DyingCreatureControllerMaySearchLibraryForSameNameEffect`,
`EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect`, `SelfExiledWhileActivatingCraftAbilityEffect`,
`SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect`, `FirebenderAscensionEffect`, `EmblemLifeGainTriggerEffect`,
`ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect`), the converted forms, the new predicate
`CardSharesNameWithTriggeringPermanentPredicate`, the triggers `Triggers.exiledWhileActivatingCraftAbility()` and
`Triggers.instantOrSorceryYouControlDealsDamageToPlayer()`, `CopyTriggeringSpellOrAbilityEffect` copying a triggered
ability, the emblems' "whenever you gain life" abilities, and `actsOnlyOnItsTargets()` on the graveyard return and the
targeted tap. Also drops a corrupted duplicate Firebender Ascension row of `ORACLE_TEXT_EFFECT_MAP.md` (Jalira's row is
already there on its own).

Whole-line edits keyed by the line's start (the old line is replaced, or a new line inserted after an anchor line),
or a fragment replaced inside a line, so a second run changes nothing. Fails if a doc or skill still names a removed
record outside a "Gone" line.

Usage: step15-run17z6-docs.py
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RUN = "effect-slot-refactor.md step 15, run 17z6"
QR = "agent-docs/EFFECTS_QUICK_REFERENCE.md"
IX = "agent-docs/EFFECTS_INDEX.md"
OM = "agent-docs/ORACLE_TEXT_EFFECT_MAP.md"
PR = "agent-docs/PREDICATES_REFERENCE.md"
AG = "agent-docs/ACTIVATED_ABILITY_GUIDE.md"
TS = "agent-docs/TRIGGER_SLOT_TARGETING.md"
CA = "agent-docs/CARD_PATTERNS_PERMANENTS_ARTIFACTS.md"
CT = "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md"
CW = "agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md"


def gone(now):
    return f"**Gone** ({RUN}): {now}"


MONKEY = ("`SequenceEffect.of(new SacrificeSelfEffect(), new CreateTokenEffect(\"Monkey\", 2, 2, GREEN, …)"
          ".withAmount(new TriggeringPermanentManaValue()))` on `Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER)`")
VERDANT = ("`MayEffect(new SearchLibraryEffect(new Fixed(1), new CardSharesNameWithTriggeringPermanentPredicate(), "
           "LibrarySearchDestination.BATTLEFIELD, LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER), prompt, null, "
           "MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)`")
PATTERN = ("`MayEffect(new SearchLibraryEffect(new Fixed(1), new CardTypePredicate(CardType.CREATURE), "
           "LibrarySearchDestination.BATTLEFIELD, LibrarySearchPlayer.TRIGGERING_PERMANENT_CONTROLLER), prompt, null, "
           "MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER)`")
FIREBENDER = ("`Triggers.attackingCreatureYouControlAbilityTriggers()` + `PutCountersOnSourceCardEffect(QUEST)` + "
              "`ConditionalEffect(new SourceCounterThreshold(4, QUEST), new MayEffect(new "
              "CopyTriggeringSpellOrAbilityEffect(), prompt))`")
DELLIAN = ("`CreateEmblemEffect(new TriggeredAbility(Triggers.youGainLife(), new LoseLifeEffect(new EventValue(), "
           "LoseLifeRecipient.TARGET_PLAYER)).withTargetFilter(new PlayerPredicateTargetFilter(new "
           "PlayerRelationPredicate(PlayerRelation.OPPONENT), \"Target must be an opponent\")), \"…\")`")
CARAH = ("`Triggers.instantOrSorceryYouControlDealsDamageToPlayer()` + `ExileTopCardMayPlayThisTurnEffect(false)`")
GNOME = "`Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS)` + the card's own effect"

EDITS = [
    (OM, "| \"When a creature enters, sacrifice this artifact and create X [token] tokens,",
     "| \"When a creature enters, sacrifice this artifact and create X [token] tokens, where X is that creature's mana "
     "value.\" | " + MONKEY + " | trigger | Monkey Cage. One ability of one effect; the tokens are created whether or "
     "not the sacrifice happened (no \"if you do\"), X read as the ability resolves from the entering creature as it "
     "is, else as it last existed (CR 608.2h) |", "replace"),
    (OM, "| \"Whenever a creature you control attacking causes a triggered ability of that creature to trigger,",
     "| \"Whenever a creature you control attacking causes a triggered ability of that creature to trigger, put a quest "
     "counter on this enchantment. Then if it has four or more quest counters on it, you may copy that ability.\" | "
     + FIREBENDER + " | trigger | Firebender Ascension. `TriggerCollectionService.checkAttackingCreatureTriggeredAbility"
     "Triggers` puts the ability on the stack above the ability that made it trigger, a snapshot of that ability the "
     "entry's triggering spell snapshot, which the copy reads |", "replace"),
    (OM, "| \"Whenever ~ or an instant or sorcery spell you control deals damage to a player, exile the top card",
     "| \"Whenever ~ or an instant or sorcery spell you control deals damage to a player, exile the top card of your "
     "library. You may play that card this turn\" | `Triggers.dealsDamageToPlayer()` + "
     "`ExileTopCardMayPlayThisTurnEffect(false)` for the source's own damage; " + CARAH + " for controlled instant or "
     "sorcery damage | trigger | Syr Carah, the Bold. The spell trigger goes on the stack once for each player the "
     "spell dealt damage to, its controller included (CR 603.2c) |", "replace"),
    (OM, "| \"Whenever a green nontoken creature dies, that creature's controller may search their library for a card with the same name",
     "| \"Whenever a green nontoken creature dies, that creature's controller may search their library for a card with "
     "the same name as that creature, put it onto the battlefield, then shuffle\" | " + VERDANT + " | "
     "`Triggers.creatureDies(PermanentAllOfPredicate(PermanentColorInPredicate(Set.of(GREEN)), Not(IsToken)), ANY)` | "
     "Verdant Succession (ODY). The name is the entry's triggering permanent's, read as the ability resolves "
     "(`AmountEvaluationService.bindTriggeringPermanentName`, the creature as it last existed) |", "replace"),
    (OM, "| \"When enchanted creature dies, that creature's controller may search their library for a creature card, put that card",
     "| \"When enchanted creature dies, that creature's controller may search their library for a creature card, put "
     "that card onto the battlefield, then shuffle\" | " + PATTERN + " | `Triggers.dies(ObjectRelation.ENCHANTED)` | "
     "Pattern of Rebirth. The enchanted-permanent death collector records the creature's controller as it last existed "
     "as the entry's triggering permanent controller, who is asked and searches |", "replace"),
    (OM, "| \"When this creature is exiled from the battlefield while you're activating a craft ability,",
     "| \"When this creature is exiled from the battlefield while you're activating a craft ability, you gain 1 life and "
     "draw a card.\" | " + GNOME + " (`SequenceEffect.of(GainLifeEffect(1), DrawCardEffect())`) | trigger | Market "
     "Gnome. A move from the battlefield to exile caused by paying a craft cost (`ZoneChangeCause.CRAFT`); deferred "
     "with the other activation-cost triggers, so it resolves above the craft ability |", "replace"),
    (QR, "- `SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect(CreateTokenEffect tokenTemplate)` -",
     "- `SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect` — " + gone("Monkey Cage writes " + MONKEY),
     "replace"),
    (QR, "- `FirebenderAscensionEffect()` —",
     "- `FirebenderAscensionEffect` — " + gone("Firebender Ascension writes " + FIREBENDER), "replace"),
    (QR, "- `EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect()` —",
     "- `EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect` — "
     + gone("Pattern of Rebirth writes " + PATTERN), "replace"),
    (QR, "- `DyingCreatureControllerMaySearchLibraryForSameNameEffect()` —",
     "- `DyingCreatureControllerMaySearchLibraryForSameNameEffect` — " + gone("Verdant Succession writes " + VERDANT),
     "replace"),
    (QR, "- `ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect()` —",
     "- `ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect` — "
     + gone("Syr Carah, the Bold writes " + CARAH), "replace"),
    (QR, "- `EmblemLifeGainTriggerEffect` (capability interface;",
     "- `EmblemLifeGainTriggerEffect` — " + gone("an emblem's \"whenever you gain life\" ability is a `TriggeredAbility` "
     "of the emblem on `Triggers.youGainLife()` (Professor Dellian Fel: " + DELLIAN + "), read by "
     "`TriggerCollectionService.collectEmblemLifeGainTriggers`, \"that much\" the entry's event value, a target chosen "
     "as it goes on the stack"), "replace"),
    (QR, "- `SelfExiledWhileActivatingCraftAbilityEffect(CardEffect wrapped)` —",
     "- `SelfExiledWhileActivatingCraftAbilityEffect` — " + gone("Market Gnome writes " + GNOME), "replace"),
    (QR, "- `CopyTriggeringSpellOrAbilityEffect()` —", None,
     "fragment:copies the stack entry's triggering spell, or the activated ability an activation trigger was about,=>"
     "copies the stack entry's triggering spell, or the activated ability an activation trigger was about, or the "
     "triggered ability of an attacking creature that triggered Firebender Ascension (the entry's triggering spell "
     "snapshot, a `TRIGGERED_ABILITY` entry, copied through `CopyTriggeredAbilityFromSnapshotEffectHandler`),"),
    (IX, "| `FirebenderAscensionEffect` |",
     "| `FirebenderAscensionEffect` | — | " + gone("Firebender Ascension writes " + FIREBENDER) + " |", "replace"),
    (IX, "| `SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect` |",
     "| `SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect` | — | " + gone("Monkey Cage writes " + MONKEY)
     + " |", "replace"),
    (IX, "| `EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect` |",
     "| `EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect` | — | "
     + gone("Pattern of Rebirth writes " + PATTERN) + " |", "replace"),
    (IX, "| `ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect` |",
     "| `ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect` | — | "
     + gone("Syr Carah, the Bold writes " + CARAH) + " |", "replace"),
    (IX, "| `EmblemLifeGainTriggerEffect` |",
     "| `EmblemLifeGainTriggerEffect` | — | " + gone("Professor Dellian Fel's emblem is " + DELLIAN) + " |", "replace"),
    (IX, "| `SelfExiledWhileActivatingCraftAbilityEffect` |",
     "| `SelfExiledWhileActivatingCraftAbilityEffect` | — | " + gone("Market Gnome writes " + GNOME) + " |", "replace"),
    (IX, "| `DyingCreatureControllerMaySearchLibraryForSameNameEffect` |",
     "| `DyingCreatureControllerMaySearchLibraryForSameNameEffect` | — | " + gone("Verdant Succession writes " + VERDANT)
     + " |", "after-anchor:| `EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect` |"),
    (IX, "| `CopyTriggeringSpellOrAbilityEffect` |", None,
     "fragment: for this ability's controller.=> for this ability's controller, or the triggered ability of an "
     "attacking creature that triggered Firebender Ascension (`Triggers.attackingCreatureYouControlAbilityTriggers()`, "
     "the entry's triggering spell snapshot a `TRIGGERED_ABILITY` entry)."),
    (PR, "| `CardSharesNameWithTriggeringPermanentPredicate` |",
     "| `CardSharesNameWithTriggeringPermanentPredicate` | `()` | a card with the same name as the permanent that "
     "triggered the resolving ability, read as it resolves (the permanent as it is, else as it last existed on the "
     "battlefield, CR 608.2h; a face-down one has no name). `SearchLibraryEffect`'s filter reads it through "
     "`AmountEvaluationService.bindTriggeringPermanentName` (Verdant Succession's \"a card with the same name\"); it "
     "matches nothing elsewhere |", "after-anchor:| `CardSharesNameWithTriggeringSpellPredicate` |"),
    (AG, "| `Triggers.instantOrSorceryYouControlDealsDamageToPlayer()` |",
     "| `Triggers.instantOrSorceryYouControlDealsDamageToPlayer()` | \"Whenever an instant or sorcery spell you control "
     "deals damage to a player, ...\" — once for each player the spell dealt damage to, its controller included "
     "(CR 603.2c), that player the stack entry's triggering player and the damage dealt to them its `eventValue`. "
     "Syr Carah, the Bold |",
     "after-anchor:| `Triggers.instantOrSorceryYouControlDealsDamageToOpponent()` |"),
    (TS, "| `Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS)` |",
     "| `Triggers.exiledWhileActivatingCraftAbility(ObjectRelation.THIS)` | The source's own move from the battlefield to "
     "exile caused by paying a craft cost (`ZoneChangeCause.CRAFT`, as the permanent with craft or a material), "
     "collected by the leave pipeline and deferred with the activation's cost triggers (Market Gnome). | Non-targeting |",
     "after-anchor:| `Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD), null, ObjectRelation.ANOTHER).causedBy(PlayerRelation.YOU)` |"),
    (TS, "- **\"My optional ability asks its question though no target was chosen.\"**", None,
     "fragment:(`AttachTargetEquipmentToTriggeringPermanentEffect`) when the entry has no target at all=>"
     "(`AttachTargetEquipmentToTriggeringPermanentEffect`, `ReturnTargetCardsFromGraveyardToHandEffect`, "
     "`TapPermanentsEffect(TARGET)`) when the entry has no target at all: no permanent or player and no card"),
    (CA, "| Creature-entry sacrifice + mana-value-scaled tokens | `m/MonkeyCage.java` |",
     "| Creature-entry sacrifice + mana-value-scaled tokens | `m/MonkeyCage.java` | " + MONKEY + " — X is the entering "
     "creature's mana value as the ability resolves (as it last existed if it left), the tokens created whether or not "
     "the sacrifice happened |", "replace"),
    (CT, "| Enchanted creature dies — its controller may search for a creature (Aura) | `p/PatternOfRebirth.java` |",
     "| Enchanted creature dies — its controller may search for a creature (Aura) | `p/PatternOfRebirth.java` | "
     "Triggers.dies(ObjectRelation.ENCHANTED) " + PATTERN + " — the dying creature's controller, who may differ from "
     "the Aura's controller, is asked and searches |", "replace"),
    (CW, "| Life-gain trigger emblem | `p/ProfessorDellianFel.java` |",
     "| Life-gain trigger emblem | `p/ProfessorDellianFel.java` | " + DELLIAN + "; the emblem life-gain reader puts it "
     "on the stack with the life gained as the event value and the target opponent chosen as it goes on the stack |",
     "replace"),
]

BLOCKS = [
    (OM, '| "Whenever a creature you control attacking causes a triggered ability of that creature to trigger, put a quest counter on this enchantment. Then if it has four or more quest counters on it, you may copy that ability." | `STATIC FirebenderAscensionEffect()` collected with `TriggerContext.AttackingCreatureTriggeredAbility`, resolving `PutCountersOnSourceCardEfestOnBottomRandomEffect(CardAllOfPredicate(CREATURE, CardNotPredicate(LEGENDARY)), LibrarySearchDestination.BATTLEFIELD)` | activated ability | Jalira, Master Polymorphist. The predicate excludes legendary creature cards; all revealed cards other than the found creature are bottomed immediately in random order. |' + "\n", ""),
]

GONE = ["DyingCreatureControllerMaySearchLibraryForSameNameEffect",
        "EnchantedCreatureControllerMaySearchLibraryForCreatureToBattlefieldEffect",
        "SelfExiledWhileActivatingCraftAbilityEffect", "SacrificeSelfThenCreateTokensEqualToEnteringManaValueEffect",
        "FirebenderAscensionEffect", "EmblemLifeGainTriggerEffect",
        "ExileTopCardMayPlayThisTurnWhenInstantOrSorceryDealsDamageToPlayerEffect", "capturesTriggeringAuraManaValue"]


def apply(lines, start, new, mode):
    idx = [i for i, line in enumerate(lines) if line.startswith(start)]
    if mode == "delete":
        if not idx:
            return False
        del lines[idx[0]]
        return True
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
    for path, old, new in BLOCKS:
        file = ROOT / path
        text = file.read_text()
        if old not in text:
            if new and new not in text:
                raise SystemExit(f"block not found in {path}: {old[:60]}")
            continue
        file.write_text(text.replace(old, new))
        print(f"updated {path}")
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
