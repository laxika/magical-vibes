#!/usr/bin/env python3
"""Step 15, run 17z13e: "whenever enchanted permanent becomes tapped" gets a default that records the event.

The watch had no default: eleven class-keyed handlers each recorded a different part of the event (the tapped
permanent, its controller, the controller or the permanent baked into `targetId`, nothing) and an effect no handler
named didn't trigger. `MiscTriggerCollectorService.handleEnchantedPermanentTapDefault` records the tapped permanent
and its controller, the controller as the non-targeting "that player" and the source snapshot for every ability
(a targeted one through its target choice, carrying the same record); Relic Bind's modal keeps its mode-choice route.
The handlers go with their annotations and Javadoc. Idempotent.
"""
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from step15_handler_removal import has_method, remove_method  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
PATH = ROOT / ("magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger/"
               "MiscTriggerCollectorService.java")
REMOVE = [
    "handleEnchantedPermanentTapPoison", "handleEnchantedPermanentTapDestroy",
    "handleEnchantedPermanentTapRemoveCounter", "handleEnchantedPermanentTapCounter",
    "handleEnchantedPermanentTapDamage", "handleEnchantedPermanentTapForcedCost",
    "handleEnchantedPermanentTapLoseLife", "handleEnchantedPermanentTapMill", "handleEnchantedPermanentTapDraw",
    "handleEnchantedPermanentTapBoostTarget", "handleEnchantedPermanentTapSequence",
]
DEFAULT = '''
    /**
     * "Whenever enchanted permanent becomes tapped": the ability is the Aura's (CR 113.7), and its entry records the
     * tapped permanent and its controller whatever its effects are ("destroy it", "its controller loses 2 life", "that
     * player mills three cards", read as the ability resolves, else from its last known information), the controller as
     * the non-targeting "that player" and the Aura as it was. A targeted ability's target is chosen as it goes on the
     * stack (CR 603.3d), the choice carrying the same record.
     */
    @CollectsTapTrigger(value = CardEffect.class, watch = TapWatch.ON_ENCHANTED_PERMANENT_TAPPED)
    private boolean handleEnchantedPermanentTapDefault(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        TriggerContext.EnchantedPermanentTap ept = (TriggerContext.EnchantedPermanentTap) ctx;
        Card sourceCard = match.permanent().getCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    "enchanted permanent became tapped", match.permanent().getId(),
                    new Permanent(match.permanent()), null, false)
                    .withTriggeringPermanent(ept.tappedPermanent()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s triggered ability",
                    new ArrayList<>(List.of(effect)),
                    ept.tappedPermanentControllerId(),
                    match.permanent().getId());
            entry.setNonTargeting(true);
            entry.setTriggeringPermanentId(ept.tappedPermanent().getId());
            entry.setTriggeringPermanentControllerId(ept.tappedPermanentControllerId());
            entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
            match.gameData().enqueueTrigger(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers on enchanted permanent tap ({})",
                match.gameData().id, sourceCard.getName(), ept.tappedPermanent().getCard().getName());
        return true;
    }
'''
ANCHOR = "\n    /**\n     * \"Whenever enchanted artifact becomes tapped, choose one —\" (Relic Bind)"

text = PATH.read_text()
removed = 0
for name in REMOVE:
    if has_method(text, name):
        text = remove_method(text, name)
        removed += 1
if "handleEnchantedPermanentTapDefault" not in text:
    assert ANCHOR in text
    text = text.replace(ANCHOR, DEFAULT + ANCHOR, 1)
text = text.replace("\n    // ── ON_ENCHANTED_PERMANENT_TAPPED ──────────────────────────────────\n", "\n")
PATH.write_text(text)
print(removed, "removed")
