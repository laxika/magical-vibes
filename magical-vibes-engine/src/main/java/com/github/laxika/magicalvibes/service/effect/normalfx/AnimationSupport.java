package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Shared animation/transform helpers used by every "normal" Animation effect handler and by
 * {@code MultiPermanentChoiceHandlerService} (async transform-and-attach re-entry).
 *
 * <p>Extracted verbatim from the original {@code AnimationResolutionService} monolith;
 * behavior (log strings, trigger order) is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnimationSupport {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final CreatureControlService creatureControlService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    /**
     * SELF/TARGET scope, until end of turn (manlands, Crew, Chimeric Staff/Mass, Warden of the Wall).
     * A {@code null} power/toughness means "use the source's printed value" (Crew on Vehicles).
     */
    public void animateSingle(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (self == null) {
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, self);
        int power = effect.power() == null
                ? (self.getCard().getPower() != null ? self.getCard().getPower() : 0)
                : amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = effect.toughness() == null
                ? (self.getCard().getToughness() != null ? self.getCard().getToughness() : 0)
                : amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        boolean untilEndOfCombat = effect.duration() == EffectDuration.UNTIL_END_OF_COMBAT;
        if (untilEndOfCombat) {
            self.setAnimatedUntilEndOfCombat(true);
        } else {
            self.setAnimatedUntilEndOfTurn(true);
        }
        self.setAnimatedPower(power);
        self.setAnimatedToughness(toughness);
        self.setAnimatedColor(effect.animatedColor());
        self.getTransientSubtypes().clear();
        self.getTransientSubtypes().addAll(effect.grantedSubtypes());
        self.getGrantedKeywords().addAll(effect.grantedKeywords());
        self.getGrantedCardTypes().addAll(effect.grantedCardTypes());

        String durationText = untilEndOfCombat ? "until end of combat" : "until end of turn";
        String logEntry = self.getCard().getName() + " becomes a " + power + "/" + toughness + " creature " + durationText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), power, toughness);
    }

    /** OWN_LANDS scope — all lands you control (Sylvan Awakening), until end of turn or your next turn. */
    public void animateOwnLands(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        boolean untilNextTurn = effect.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN;

        for (Permanent perm : battlefield) {
            if (!perm.getCard().hasType(CardType.LAND)) {
                continue;
            }

            if (untilNextTurn) {
                perm.setAnimatedUntilNextTurn(true);
                perm.setUntilNextTurnAnimatedPower(power);
                perm.setUntilNextTurnAnimatedToughness(toughness);
                perm.getUntilNextTurnSubtypes().clear();
                perm.getUntilNextTurnSubtypes().addAll(effect.grantedSubtypes());
                perm.getUntilNextTurnKeywords().addAll(effect.grantedKeywords());
            } else {
                perm.setAnimatedUntilEndOfTurn(true);
                perm.setAnimatedPower(power);
                perm.setAnimatedToughness(toughness);
                perm.setAnimatedColor(effect.animatedColor());
                perm.getTransientSubtypes().clear();
                perm.getTransientSubtypes().addAll(effect.grantedSubtypes());
                perm.getGrantedKeywords().addAll(effect.grantedKeywords());
                perm.getGrantedCardTypes().addAll(effect.grantedCardTypes());
            }

            log.info("Game {} - {} animated{}", gameData.id, perm.getCard().getName(),
                    untilNextTurn ? " until next turn" : " until end of turn");
        }

        String durationText = untilNextTurn ? "until your next turn" : "until end of turn";
        gameBroadcastService.logAndBroadcast(gameData,
                "All lands you control become " + power + "/" + toughness
                        + " Elemental creatures with reach, indestructible, and haste " + durationText + ". They're still lands.");
    }

    /** ALL_LANDS scope — every land on the battlefield (both players), until end of turn (Natural Affinity). */
    public void animateAllLands(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect) {
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent perm : battlefield) {
                if (!perm.getCard().hasType(CardType.LAND)) {
                    continue;
                }
                perm.setAnimatedUntilEndOfTurn(true);
                perm.setAnimatedPower(power);
                perm.setAnimatedToughness(toughness);
                perm.setAnimatedColor(effect.animatedColor());
                perm.getTransientSubtypes().clear();
                perm.getTransientSubtypes().addAll(effect.grantedSubtypes());
                perm.getGrantedKeywords().addAll(effect.grantedKeywords());
                perm.getGrantedCardTypes().addAll(effect.grantedCardTypes());

                log.info("Game {} - {} animated until end of turn", gameData.id, perm.getCard().getName());
            }
        }

        gameBroadcastService.logAndBroadcast(gameData,
                "All lands become " + power + "/" + toughness
                        + " creatures until end of turn. They're still lands.");
    }

    /**
     * OWN_PERMANENTS scope — all permanents you control matching the filter become artifact
     * creatures until end of turn (The Antiquities War chapter III).
     */
    public void animateControlledPermanents(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        AmountContext ctx = AmountContext.forStackEntry(entry, source);
        int power = amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        int count = 0;
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, effect.filter())) {
                permanent.setAnimatedUntilEndOfTurn(true);
                permanent.setAnimatedPower(power);
                permanent.setAnimatedToughness(toughness);
                permanent.getGrantedCardTypes().add(CardType.CREATURE);

                // Per MTG rules: if an Equipment becomes a creature, it becomes unattached (CR 301.5c)
                if (permanent.isAttached() && permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                    permanent.setAttachedTo(null);
                    String unattachLog = permanent.getCard().getName() + " becomes unattached.";
                    gameBroadcastService.logAndBroadcast(gameData, unattachLog);
                }
                count++;
            }
        }

        String logEntry = count + " artifact(s) become " + power + "/" + toughness + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} artifacts animated as {}/{} creatures until end of turn",
                gameData.id, count, power, toughness);
    }

    /** TARGET scope, PERMANENT duration — target permanent becomes a creature with no wear-off (Tezzeret, Waker). */
    public void animatePermanentTarget(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, target);
        int power = amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        target.setPermanentlyAnimated(true);
        target.setPermanentAnimatedPower(power);
        target.setPermanentAnimatedToughness(toughness);

        for (CardSubtype subtype : effect.grantedSubtypes()) {
            if (!target.getGrantedSubtypes().contains(subtype)) {
                target.getGrantedSubtypes().add(subtype);
            }
        }

        target.getGrantedKeywords().addAll(effect.grantedKeywords());

        // Per MTG rules: if an Equipment becomes a creature, it becomes unattached (CR 301.5c)
        if (target.isAttached() && target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            target.setAttachedTo(null);
            String unattachLog = target.getCard().getName() + " becomes unattached.";
            gameBroadcastService.logAndBroadcast(gameData, unattachLog);
            log.info("Game {} - {} unattached (equipment became creature)", gameData.id, target.getCard().getName());
        }

        String logEntry = target.getCard().getName() + " becomes a " + power + "/" + toughness + " creature.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature permanently", gameData.id, target.getCard().getName(), power, toughness);
    }

    /**
     * TARGET scope, WHILE_SOURCE_ON_BATTLEFIELD duration — target land becomes a creature for as
     * long as the source permanent remains on the battlefield (Awakener Druid). It's still a land.
     */
    public void animateWhileSource(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        // Per ruling: if the source creature left the battlefield before this ETB resolves,
        // nothing happens to the targeted land.
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null || gameQueryService.findPermanentById(gameData, sourcePermanentId) == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability has no effect (it is no longer on the battlefield).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {} ETB has no effect, source left battlefield", gameData.id, entry.getCard().getName());
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, target);
        int power = amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        target.setPermanentlyAnimated(true);
        target.setPermanentAnimatedPower(power);
        target.setPermanentAnimatedToughness(toughness);

        for (CardSubtype subtype : effect.grantedSubtypes()) {
            if (!target.getGrantedSubtypes().contains(subtype)) {
                target.getGrantedSubtypes().add(subtype);
            }
        }

        if (effect.animatedColor() != null) {
            target.getGrantedColors().add(effect.animatedColor());
        }

        gameData.sourceLinkedAnimations.put(target.getId(), sourcePermanentId);

        String logEntry = target.getCard().getName() + " becomes a " + power + "/" + toughness
                + " green Treefolk creature. It's still a land.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature while {} is on the battlefield",
                gameData.id, target.getCard().getName(), power, toughness,
                entry.getCard().getName());
    }

    /**
     * Completes Soul Seizer-style transform-and-attach after the controller chooses a target creature.
     */
    public void completeTransformAndAttach(GameData gameData, UUID controllerId, UUID sourcePermId, UUID targetPermId) {
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermId);
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermId);
        if (source == null) {
            String logEntry = "Transform-and-attach fizzles — source no longer on the battlefield.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            String logEntry = source.getCard().getName() + "'s ability fizzles — target creature no longer exists.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            return;
        }

        if (!transformToBackFace(gameData, source)) {
            return;
        }

        source.setAttachedTo(target.getId());
        String attachLog = source.getCard().getName() + " is attached to " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, attachLog);
        log.info("Game {} - {} attached to {}", gameData.id, source.getCard().getName(), target.getCard().getName());

        boolean hasControlEffect = source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
        if (hasControlEffect) {
            creatureControlService.stealPermanent(gameData, controllerId, target);
        }
    }

    public boolean transformToBackFace(GameData gameData, Permanent self) {
        Card originalCard = self.getOriginalCard();
        Card backFace = originalCard.getBackFaceCard();
        if (backFace == null) {
            log.warn("Game {} - {} has no back face to transform to", gameData.id, self.getCard().getName());
            return false;
        }

        if (gameQueryService.isTransformPrevented(gameData, self)) {
            log.info("Game {} - {} can't transform (transform prevented)", gameData.id, self.getCard().getName());
            return false;
        }

        String frontName = self.getCard().getName();
        if (self.isAttached() && !backFace.getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            self.setAttachedTo(null);
            String unattachLog = frontName + " becomes unattached.";
            gameBroadcastService.logAndBroadcast(gameData, unattachLog);
            log.info("Game {} - {} unattached (transformed into non-Equipment)", gameData.id, frontName);
        }

        self.setCard(backFace);
        self.setTransformed(true);
        String logEntry = frontName + " transforms into " + backFace.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());

        fireTransformTriggers(gameData, self, backFace, EffectSlot.ON_TRANSFORM_TO_BACK_FACE);
        return true;
    }

    public void transformToFrontFace(GameData gameData, Permanent self) {
        Card originalCard = self.getOriginalCard();
        String backName = self.getCard().getName();
        self.setCard(originalCard);
        self.setTransformed(false);
        String logEntry = backName + " transforms into " + originalCard.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());

        fireTransformTriggers(gameData, self, originalCard, EffectSlot.ON_TRANSFORM_TO_FRONT_FACE);
    }

    /**
     * Fires triggered abilities from transform trigger slots after a permanent transforms.
     */
    private void fireTransformTriggers(GameData gameData, Permanent self, Card triggerCard, EffectSlot slot) {
        List<CardEffect> effects = triggerCard.getEffects(slot);
        if (effects.isEmpty()) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, self.getId());
        if (controllerId == null) {
            return;
        }

        for (CardEffect e : effects) {
            if (e instanceof MayEffect may) {
                gameData.queueMayAbility(triggerCard, controllerId, may, null, self.getId());
                String triggerLog = triggerCard.getName() + "'s transform ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} transform trigger queued (may ability)", gameData.id, triggerCard.getName());
            } else if (e instanceof DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.TransformOpponentThenCreatureTarget(
                                triggerCard, controllerId, effects, self.getId()));
                List<UUID> opponents = gameData.orderedPlayerIds.stream()
                        .filter(pid -> !pid.equals(controllerId))
                        .toList();
                playerInputService.beginAnyTargetChoice(gameData, controllerId, List.of(), opponents,
                        triggerCard.getName() + "'s ability - Choose target opponent.");
                String triggerLog = triggerCard.getName() + "'s transform ability triggers - choose target opponent.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} transform trigger awaiting opponent target", gameData.id, triggerCard.getName());
                return;
            } else {
                gameData.stack.add(new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        triggerCard,
                        controllerId,
                        triggerCard.getName() + "'s transform ability",
                        effects,
                        null,
                        self.getId()
                ));
                String triggerLog = triggerCard.getName() + "'s transform ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} transform trigger pushed onto stack", gameData.id, triggerCard.getName());
                return;
            }
        }
    }
}
