#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 3: agent-docs for the departed-object effects that read the stack entry.

Exact line replacements keyed by each line's unique prefix; idempotent (a line already rewritten is left alone).

    python3 scripts/step15-departed-object-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path("agent-docs")

# (file, unique prefix of the old line, new line or None to delete it)
EDITS = [
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"When this creature leaves the battlefield, put its [counter type] counters on target creature you control\" | `target(TargetFilters.creatureYouControl()).addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new PutCounterOnTargetForEachLeavingSourceCounterEffect(",
     "| \"When this creature leaves the battlefield, put its [counter type] counters on target creature you control\" | `target(TargetFilters.creatureYouControl()).addTrigger(Triggers.leavesBattlefield(ObjectRelation.THIS), new PutCounterOnTargetPermanentEffect(CounterType.X, new CountersOnTriggeringPermanent(CounterType.X)))` | `Triggers.leavesBattlefield(THIS)` | The count is the leaving permanent's counters of that type as it last existed on the battlefield (the stack entry's triggering permanent), read as the ability resolves; the trigger still targets when it had none (Selfless Police Captain) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "- \"When this creature dies, exile it if it had a death counter on it.",
     "- \"When this creature dies, exile it if it had a death counter on it. Otherwise, return it to the battlefield under your control and put a death counter on it.\" -> `Triggers.dies(THIS)` + `ExileIfHadCounterElseReturnWithCounterEffect(CounterType.DEATH)` (Bogardan Phoenix): whether it had the counter is read from the stack entry's triggering permanent as the ability resolves."),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever another creature you own dies, return it to your hand unless target opponent pays 3 life\" |",
     "| \"Whenever another creature you own dies, return it to your hand unless target opponent pays 3 life\" | `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(3)` | `Triggers.creatureDies(ObjectRelation.YOU_OWN)` | Athreos, God of Passage. The ownership-based slot watches the source controller's graveyard, the target opponent is chosen as the trigger is put on the stack, and payment is offered when the trigger resolves. \"It\" is the stack entry's triggering card; a token or card no longer in its owner's graveyard cannot return. |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a creature an opponent controls with a -1/-1 counter on it dies, you may return that card to the battlefield under your control\" |",
     "| \"Whenever a creature an opponent controls with a -1/-1 counter on it dies, you may return that card to the battlefield under your control\" | `Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.MINUS_ONE_MINUS_ONE), OPPONENT_CONTROLS)` + `new MayEffect(new ReturnDyingOpponentCreatureUnderYourControlEffect(), \"…\")` | trigger | Necroskitter. The filter is judged on the dying permanent; \"that card\" is the stack entry's triggering card, and the \"you may\" is asked as the ability resolves (CR 603.5) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a creature an opponent controls dies, you may pay {1}{B}. If you do, return that card to the battlefield under your control.",
     "| \"Whenever a creature an opponent controls dies, you may pay {1}{B}. If you do, return that card to the battlefield under your control. If it's a creature, it's a Zombie in addition to its other creature types\" | `Triggers.creatureDies(OPPONENT_CONTROLS)` + `new MayPayManaEffect(\"{1}{B}\", new ReturnDyingOpponentCreatureUnderYourControlEffect(CardSubtype.ZOMBIE), \"…\")` | trigger | Lim-Dûl the Necromancer. \"That card\" is the stack entry's triggering card; Zombie is granted only when the returned permanent is a creature |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a creature is put into an opponent's graveyard from the battlefield, you gain life equal to its toughness\" |",
     "| \"Whenever a creature is put into an opponent's graveyard from the battlefield, you gain life equal to its toughness\" | `GainLifeEffect(new TriggeringPermanentToughness())` | `Triggers.dies(Triggers.CREATURE, ObjectRelation.OPPONENT_OWNS)` | Grim Feast. The collector records the creature's last-known effective toughness on the stack entry, which `TriggeringPermanentToughness` reads as the ability resolves |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a creature you control dies, you gain life equal to its toughness\" |",
     "| \"Whenever a creature you control dies, you gain life equal to its toughness\" | `GainLifeEffect(new TriggeringPermanentToughness())` | `Triggers.creatureDies(YOU_CONTROL)` | Proper Burial (South Wind Avatar: `ANOTHER_YOU_CONTROL`). The ally-death collector records the creature's last-known effective toughness on the stack entry |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"When this dies, if it had one or more counters on it, create a 0/0 [color] [Subtype] token, then put this creature's counters on that token\" |",
     "| \"When this dies, if it had one or more counters on it, create a 0/0 [color] [Subtype] token, then put this creature's counters on that token\" | `CreateTokenWithDyingSourceCountersEffect(new CreateTokenEffect(\"name\", 0, 0, color, Set.of(colors), List.of(subtype)))` | `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)` | The intervening \"if\" is the trigger's filter; the token gets every counter type the stack entry's triggering permanent had (e.g. Ambitious Augmenter's Fractal) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"When this creature dies, draw a card for each -1/-1 counter on it\" |",
     "| \"When this creature dies, draw a card for each -1/-1 counter on it\" | `DrawCardEffect(new CountersOnTriggeringPermanent(CounterType.MINUS_ONE_MINUS_ONE))` | `Triggers.dies(THIS)` | The count is read from the stack entry's triggering permanent, the creature as it last existed on the battlefield (e.g. Dusk Urchins). Pair with `Triggers.attacks()`/`Triggers.blocks()` `PutCountersOnSourceEffect(-1,-1,1)` for \"whenever it attacks or blocks, put a -1/-1 counter on it\" |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a legendary creature with counters on it dies, draw a card for each counter on it. You lose 2 life\" |",
     "| \"Whenever a legendary creature with counters on it dies, draw a card for each counter on it. You lose 2 life\" | `Triggers.creatureDies(PermanentAllOfPredicate(IsCreature, HasSupertype(LEGENDARY), HasCounters(ANY)), ANY)` + `SequenceEffect.of(new DrawCardEffect(new CountersOnTriggeringPermanent()), new LoseLifeEffect(2))` | death trigger | The no-arg amount counts every counter on the dying creature as it last existed on the battlefield; the sequence keeps the draw and the life loss one ability (the \"a creature dies\" collector puts each effect of an ability on the stack separately). Cleopatra, Exiled Pharaoh (ACR 52) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a permanent an opponent controls is put into a graveyard, put that card onto the battlefield under your control unless that opponent pays 3 life\" |",
     "| \"Whenever a permanent an opponent controls is put into a graveyard, put that card onto the battlefield under your control unless that opponent pays 3 life\" | `StealDyingOpponentPermanentUnlessPaysLifeEffect(3)` | `Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS)` | Prince of Thralls. Fires for any permanent type from the battlefield; \"that card\" is the stack entry's triggering card and \"that opponent\" its triggering permanent's controller, who chooses pay-life vs. lose-the-permanent (can't-pay ⇒ auto-steal). Stolen via `trackStolenCreature`, so it returns to its owner on death |"),
    ("ORACLE_TEXT_EFFECT_MAP.md",
     "| \"Whenever a creature dies, you may pay {N}. If you do, this creature becomes a copy of that creature, except it has this ability\" |",
     "| \"Whenever a creature dies, you may pay {N}. If you do, this creature becomes a copy of that creature, except it has this ability\" | `MayPayManaEffect(\"{N}\", new BecomeCopyOfDyingCreatureEffect(), \"prompt\")` | `Triggers.creatureDies(ANY)` | Copies the stack entry's triggering permanent (the dying creature's last-known information) and re-grants this trigger. Cemetery Puca |"),

    ("EFFECTS_INDEX.md",
     "| `ManifestTopCardEffect` | `()` |",
     "| `ManifestTopCardEffect` | `()` / `(DynamicAmount amount)` | manifests the top `amount` cards of the controller's library one at a time (CR 701.40e) through the shared manifest service; the amount is evaluated as the effect resolves. \"Manifest a number of cards equal to the number of counters on it\" on `Triggers.dies(THIS)` = `(new CountersOnTriggeringPermanent())` (Omarthis, Ghostfire Initiate) |"),
    ("EFFECTS_INDEX.md", "| `ManifestTopCardsForEachDyingSourceCounterEffect` |", None),
    ("EFFECTS_INDEX.md", "| `PutCounterOnTargetForEachLeavingSourceCounterEffect` |", None),
    ("EFFECTS_INDEX.md", "| `GainLifeEqualToDyingCreatureToughnessEffect` |", None),
    ("EFFECTS_INDEX.md",
     "| `ReturnDyingOpponentCreatureUnderYourControlEffect` |",
     "| `ReturnDyingOpponentCreatureUnderYourControlEffect` | `(CardSubtype grantSubtype)` or `()` | Triggers.creatureDies(OPPONENT_CONTROLS): put the creature card that died (the stack entry's triggering card) from its owner's graveyard onto your battlefield, tracked as a stolen creature (permanent control). An optional subtype is granted only if the returned permanent is a creature (Lim-Dûl the Necromancer uses `ZOMBIE`). \"You may return that card\" is `new MayEffect(new ReturnDyingOpponentCreatureUnderYourControlEffect(), \"…\")` (Necroskitter, The Reaper King, No More); \"you may pay {1}{B}. If you do\" wraps it in a `MayPayManaEffect` (Lim-Dûl the Necromancer). Fizzles if the card left the graveyard |"),
    ("EFFECTS_INDEX.md",
     "| `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect` |",
     "| `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect` | `(int lifeCost)` | `Triggers.creatureDies(ObjectRelation.YOU_OWN)`: target opponent may pay `lifeCost` life; if they do, the dead card stays in its owner's graveyard, otherwise it returns to its owner's hand. The card is the stack entry's triggering card; the target is chosen as the trigger goes on the stack, the effect uses a benign player-targeting spec and fizzles if the card has moved. Used by Athreos, God of Passage |"),
    ("EFFECTS_INDEX.md",
     "| `CreateTokenWithDyingSourceCountersEffect` |",
     "| `CreateTokenWithDyingSourceCountersEffect` | `(CreateTokenEffect tokenTemplate)` | `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)` (\"if it had one or more counters on it\" is the trigger's filter): create the template token, then put on it each counter type and count the stack entry's triggering permanent had, using `PutCountersOnCreatedPermanentsEffect`. Counters are added after entry triggers inspect the token. Used by Ambitious Augmenter (0/0 Fractal) |"),
    ("EFFECTS_INDEX.md", "| `DrawCardForEachDyingSourceCounterEffect` |", None),
    ("EFFECTS_INDEX.md",
     "| `StealDyingOpponentPermanentUnlessPaysLifeEffect` |",
     "| `StealDyingOpponentPermanentUnlessPaysLifeEffect` | `(int lifeCost)` | triggered punisher (`ControlStealingEffect`, PERMANENT): a permanent of any type an opponent controls was put into a graveyard from the battlefield — that opponent may pay `lifeCost` life, else the ability's controller puts that card onto the battlefield under their control (tracked as a stolen permanent, so it returns to its owner on death). \"That card\" is the stack entry's triggering card and \"that opponent\" its triggering permanent's controller, set by the `Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS)` and `Triggers.sacrificed(NONTOKEN, OPPONENT_CONTROLS)` collectors. Handler `StealDyingOpponentPermanentUnlessPaysLifeEffectHandler` (can't-pay ⇒ immediate steal; payable ⇒ may prompt); decline branch in `MayPenaltyChoiceHandlerService`. Used by Prince of Thralls and Nazgûl Battle-Mace (3 life) |"),
    ("EFFECTS_INDEX.md",
     "| `BecomeCopyOfDyingCreatureEffect` |",
     "| `BecomeCopyOfDyingCreatureEffect` | `()` | source permanent becomes a copy of a creature that just died, copying the stack entry's triggering permanent (its last-known copiable characteristics, even after its card leaves the graveyard) and re-granting the source's own `Triggers.creatureDies(ANY)` ability (\"except it has this ability\"). Place in `Triggers.creatureDies(ANY)` wrapped in `MayPayManaEffect`. Used by Cemetery Puca |"),

    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ManifestTopCardsForEachDyingSourceCounterEffect()` -",
     "- `ManifestTopCardEffect([DynamicAmount amount])` - manifest the top card, or `amount` cards one at a time (CR 701.40e); \"equal to the number of counters on it\" on `Triggers.dies(THIS)` = `new ManifestTopCardEffect(new CountersOnTriggeringPermanent())` (Omarthis, Ghostfire Initiate)."),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `PutCounterOnTargetForEachLeavingSourceCounterEffect(",
     "- \"Put its [type] counters on target creature\" on `Triggers.leavesBattlefield(THIS)` = `PutCounterOnTargetPermanentEffect(type, new CountersOnTriggeringPermanent(type))`: the count is the leaving permanent's as it last existed on the battlefield, and the trigger still targets when it had none (Selfless Police Captain)"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(int lifeCost)` —",
     "- `ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(int lifeCost)` — Triggers.creatureDies(ObjectRelation.YOU_OWN): target opponent may pay `lifeCost` life; if they decline or cannot pay, return the owned dying creature (the stack entry's triggering card) to its owner's hand. The target is chosen as the trigger is put on the stack (Athreos, God of Passage)"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ExileIfHadCounterElseReturnWithCounterEffect(CounterType)` —",
     "- `ExileIfHadCounterElseReturnWithCounterEffect(CounterType)` — Triggers.dies(THIS): exile if the dying source had that counter, else return it to the battlefield under your control with one of that counter (Bogardan Phoenix / death counters). As it resolves it reads the stack entry's triggering permanent and resolves `ExileSourceCardFromGraveyardEffect` or `ReturnTriggeringCardFromGraveyardToBattlefieldEffect` next"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `ReturnDyingOpponentCreatureUnderYourControlEffect([CardSubtype grantSubtype])` —",
     "- `ReturnDyingOpponentCreatureUnderYourControlEffect([CardSubtype grantSubtype])` — Triggers.creatureDies(OPPONENT_CONTROLS): put the dying creature card (the stack entry's triggering card) from its owner's graveyard onto your battlefield; an optional subtype is granted only if the returned permanent is a creature. \"You may return that card\" wraps it in `MayEffect` (Necroskitter), \"you may pay {1}{B}\" in `MayPayManaEffect` (Lim-Dûl the Necromancer, `ZOMBIE`)"),
    ("EFFECTS_QUICK_REFERENCE.md", "- `GainLifeEqualToDyingCreatureToughnessEffect()` —", None),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `StealDyingOpponentPermanentUnlessPaysLifeEffect(int lifeCost)` —",
     "- `StealDyingOpponentPermanentUnlessPaysLifeEffect(int lifeCost)` — Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS) punisher (`ControlStealingEffect`): any-type opponent permanent dies; that opponent pays `lifeCost` life or you put that card onto the battlefield under your control (Prince of Thralls, 3). Can't-pay = auto-steal; \"that card\" and \"that opponent\" are the stack entry's triggering card and triggering permanent's controller"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `CreateTokenWithDyingSourceCountersEffect(CreateTokenEffect template)` —",
     "- `CreateTokenWithDyingSourceCountersEffect(CreateTokenEffect template)` — `Triggers.dies(new PermanentHasCountersPredicate(CounterType.ANY), ObjectRelation.THIS)`: create `template`, then add each counter type and count the stack entry's triggering permanent had after the token enters (Ambitious Augmenter)."),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `DrawCardForEachDyingSourceCounterEffect(",
     "- \"Draw a card for each [type] counter on it\" on `Triggers.dies(THIS)` / `Triggers.creatureDies(...)` = `DrawCardEffect(new CountersOnTriggeringPermanent(type))` (no type: every counter); a following step of the same ability goes in a `SequenceEffect.of(draw, then)` (Dusk Urchins, MINUS_ONE_MINUS_ONE; Cleopatra, all counters + life loss)"),
    ("EFFECTS_QUICK_REFERENCE.md",
     "- `BecomeCopyOfDyingCreatureEffect()` —",
     "- `BecomeCopyOfDyingCreatureEffect()` — source permanent becomes a copy of a creature that just died (the stack entry's triggering permanent, its last-known information), retaining its own death-copy trigger (\"except it has this ability\"). Cemetery Puca. Place in `Triggers.creatureDies(ANY)` wrapped in `MayPayManaEffect` for the \"you may pay {1}\" gate"),

    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Reanimate from your graveyard, then exile the linked creature on death and return the enchantment |",
     "| Reanimate from your graveyard, then exile the linked creature on death and return the enchantment | `d/DiabolicServitude.java` | Triggers.enters(ObjectRelation.THIS) targeted `ReturnCardFromGraveyardEffect` with `linkToSource(true)` + Triggers.creatureDies(ANY) `ExileTriggeringCreatureAndReturnSourceToHandEffect` (the collector matches the dying permanent ID to the source's `chosenPermanentId`; the card exiled is the stack entry's triggering card) + Triggers.leavesBattlefield(THIS) `RemoveLinkedPermanentEffect(EXILE)` |"),
    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Opponent creature dies → gain life equal to its toughness (enchantment) |",
     "| Opponent creature dies → gain life equal to its toughness (enchantment) | `g/GrimFeast.java` | Triggers.upkeep(ActivePlayer.YOU) DealDamageToPlayersEffect(1, DamageRecipient.CONTROLLER) + Triggers.dies(Triggers.CREATURE, ObjectRelation.OPPONENT_OWNS) GainLifeEffect(new TriggeringPermanentToughness()) — the collector records the creature's last-known effective toughness on the stack entry |"),
    ("CARD_PATTERNS_CREATURES_TRIGGERED.md",
     "| Opponent creature with a -1/-1 counter dies — may reanimate under your control |",
     "| Opponent creature with a -1/-1 counter dies — may reanimate under your control | `n/Necroskitter.java` | Triggers.creatureDies(PermanentHasCountersPredicate(MINUS_ONE_MINUS_ONE), OPPONENT_CONTROLS) MayEffect(ReturnDyingOpponentCreatureUnderYourControlEffect(), \"…\") — the filter reads the dying permanent's counters; on acceptance the stack entry's triggering card is stolen from its owner's graveyard onto your battlefield (tracked as a stolen creature) |"),
    ("ACTIVATED_ABILITY_GUIDE.md",
     "| `Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS)` | A permanent of **any** type",
     "| `Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS)` | A permanent of **any** type an opponent of this permanent's controller controls is put into a graveyard from the battlefield. Fired once per removal in `PermanentRemovalService.processGraveyardAndTriggers` via `TriggerCollectionService.checkOpponentPermanentPutIntoGraveyardTriggers`; the collector gives the stack entry the card as its triggering card and that opponent as its triggering permanent's controller. Used by Prince of Thralls with `StealDyingOpponentPermanentUnlessPaysLifeEffect(3)`. |"),
    ("CARD_PATTERNS_PERMANENTS_STATIC.md",
     "| Cumulative upkeep paid by discarding per age counter + death draw |",
     "| Cumulative upkeep paid by discarding per age counter + death draw | `v/VexingSphinx.java` | Triggers.upkeep(ActivePlayer.YOU) `CumulativeUpkeepEffect.discardCard()` + Triggers.dies(THIS) `DrawCardEffect(new CountersOnTriggeringPermanent(AGE))` — the discard payment is one card per age counter; the death trigger counts the age counters it had as it last existed on the battlefield |"),
]


def main():
    by_file = {}
    for name, prefix, new in EDITS:
        by_file.setdefault(name, []).append((prefix, new))
    failed = False
    for name, edits in by_file.items():
        path = DOCS / name
        lines = path.read_text().split("\n")
        for prefix, new in edits:
            hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if new is not None and new in lines:
                continue
            if new is None and not hits:
                continue
            if len(hits) != 1:
                print("%s: %d lines start with %r" % (name, len(hits), prefix[:70]))
                failed = True
                continue
            if new is None:
                del lines[hits[0]]
            else:
                lines[hits[0]] = new
        path.write_text("\n".join(lines))
    sys.exit(1 if failed else 0)


if __name__ == "__main__":
    main()
