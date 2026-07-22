package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
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
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
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
     * CR 613.4: an animate-and-set-P/T effect's base P/T is a layer-7b entry with the
     * animation's timestamp — a later-timestamp base-P/T setter (Diminish, Lignify) overrides
     * it while the permanent stays a creature. The animation flags on {@code Permanent} are
     * still written for direct readers; this floating instance is what drives 7b precedence
     * in the layered pass ({@code agent-docs/LAYER_SYSTEM.md}).
     */
    private void addAnimationBasePtFloatingEffect(GameData gameData, StackEntry entry, Permanent target,
                                                  int power, int toughness, EffectDuration duration) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                new SetBasePowerToughnessEffect(power, toughness), target.getId(), null, null,
                duration, 0));
    }

    /**
     * SELF/TARGET scope, until end of turn (manlands, Crew, Chimeric Staff/Mass, Warden of the Wall).
     * A {@code null} power/toughness means "use the source's printed value" (Crew on Vehicles).
     *
     * <p>Supports "up to N target" abilities (Fendeep Summoner) by iterating over
     * {@code entry.getTargetIds()} when a multi-target ability populated them; a single-target
     * self/target animation still reads {@code entry.getTargetId()}.
     */
    public void animateSingle(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect) {
        List<UUID> targetIds;
        if (entry.getTargetIds() != null && !entry.getTargetIds().isEmpty()
                && (entry.getTargetIds().size() > 1 || entry.getTargetId() == null)) {
            targetIds = entry.getTargetIds();
        } else if (entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else {
            return;
        }
        for (UUID targetId : targetIds) {
            if (effect.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN) {
                animateOneUntilNextTurn(gameData, entry, effect, targetId);
            } else {
                animateOneUntilEndOfTurn(gameData, entry, effect, targetId);
            }
        }
    }

    /**
     * SELF/TARGET scope, until your next turn — the target permanent becomes a creature with the
     * given power/toughness until the controller's next turn (Xenic Poltergeist animates a
     * noncreature artifact into an artifact creature with P/T equal to its mana value). The
     * animation flag alone grants creature-ness (see {@code GameQueryService.isCreature}); the
     * layered base P/T comes from the floating 7b entry.
     */
    private void animateOneUntilNextTurn(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect, UUID targetId) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, target);
        int power = amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        target.setAnimatedUntilNextTurn(true);
        target.setUntilNextTurnAnimatedPower(power);
        target.setUntilNextTurnAnimatedToughness(toughness);
        target.getUntilNextTurnSubtypes().addAll(effect.grantedSubtypes());
        target.getUntilNextTurnKeywords().addAll(effect.grantedKeywords());
        addAnimationBasePtFloatingEffect(gameData, entry, target, power, toughness, EffectDuration.UNTIL_YOUR_NEXT_TURN);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(),
                " becomes a " + power + "/" + toughness + " creature until your next turn."));

        log.info("Game {} - {} becomes a {}/{} creature until next turn", gameData.id, target.getCard().getName(), power, toughness);
    }

    private void animateOneUntilEndOfTurn(GameData gameData, StackEntry entry, AnimatePermanentsEffect effect, UUID targetId) {
        Permanent self = gameQueryService.findPermanentById(gameData, targetId);
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
            // Deferred from the layered 7b migration: UNTIL_END_OF_COMBAT floating expiry is
            // not plumbed yet (see agent-docs/LAYER_SYSTEM.md cleanup debt) — the animation
            // stays flag-only and applies only when no layered 7b entry exists.
            self.setAnimatedUntilEndOfCombat(true);
        } else {
            self.setAnimatedUntilEndOfTurn(true);
            addAnimationBasePtFloatingEffect(gameData, entry, self, power, toughness, EffectDuration.UNTIL_END_OF_TURN);
        }
        self.setAnimatedPower(power);
        self.setAnimatedToughness(toughness);
        self.setAnimatedColor(effect.animatedColor());
        self.getTransientSubtypes().clear();
        self.getTransientSubtypes().addAll(effect.grantedSubtypes());
        self.getGrantedKeywords().addAll(effect.grantedKeywords());
        self.getGrantedCardTypes().addAll(effect.grantedCardTypes());

        String durationText = untilEndOfCombat ? "until end of combat" : "until end of turn";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(self.getCard(),
                " becomes a " + power + "/" + toughness + " creature " + durationText + "."));

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
                addAnimationBasePtFloatingEffect(gameData, entry, perm, power, toughness, EffectDuration.UNTIL_YOUR_NEXT_TURN);
            } else {
                perm.setAnimatedUntilEndOfTurn(true);
                perm.setAnimatedPower(power);
                perm.setAnimatedToughness(toughness);
                perm.setAnimatedColor(effect.animatedColor());
                perm.getTransientSubtypes().clear();
                perm.getTransientSubtypes().addAll(effect.grantedSubtypes());
                perm.getGrantedKeywords().addAll(effect.grantedKeywords());
                perm.getGrantedCardTypes().addAll(effect.grantedCardTypes());
                addAnimationBasePtFloatingEffect(gameData, entry, perm, power, toughness, EffectDuration.UNTIL_END_OF_TURN);
            }

            log.info("Game {} - {} animated{}", gameData.id, perm.getCard().getName(),
                    untilNextTurn ? " until next turn" : " until end of turn");
        }

        String durationText = untilNextTurn ? "until your next turn" : "until end of turn";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text("All lands you control become " + power + "/" + toughness
                        + " Elemental creatures with reach, indestructible, and haste " + durationText + ". They're still lands."));
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
                addAnimationBasePtFloatingEffect(gameData, entry, perm, power, toughness, EffectDuration.UNTIL_END_OF_TURN);

                log.info("Game {} - {} animated until end of turn", gameData.id, perm.getCard().getName());
            }
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text("All lands become " + power + "/" + toughness
                        + " creatures until end of turn. They're still lands."));
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
                addAnimationBasePtFloatingEffect(gameData, entry, permanent, power, toughness, EffectDuration.UNTIL_END_OF_TURN);

                // Per MTG rules: if an Equipment becomes a creature, it becomes unattached (CR 301.5c)
                if (permanent.isAttached() && permanent.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                    permanent.setAttachedTo(null);
                    gameData.expireFloatingEffectsForUnattachedSource(permanent.getId());
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(permanent.getCard(), " becomes unattached."));
                }
                count++;
            }
        }

        String logEntry = count + " artifact(s) become " + power + "/" + toughness + " creature(s) until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));

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
        addAnimationBasePtFloatingEffect(gameData, entry, target, power, toughness, EffectDuration.PERMANENT);

        for (CardSubtype subtype : effect.grantedSubtypes()) {
            if (!target.getGrantedSubtypes().contains(subtype)) {
                target.getGrantedSubtypes().add(subtype);
            }
        }

        target.getGrantedKeywords().addAll(effect.grantedKeywords());

        if (effect.animatedColor() != null) {
            target.getGrantedColors().add(effect.animatedColor());
        }

        // Per MTG rules: if an Equipment becomes a creature, it becomes unattached (CR 301.5c)
        if (target.isAttached() && target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            target.setAttachedTo(null);
            gameData.expireFloatingEffectsForUnattachedSource(target.getId());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " becomes unattached."));
            log.info("Game {} - {} unattached (equipment became creature)", gameData.id, target.getCard().getName());
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(),
                " becomes a " + power + "/" + toughness + " creature."));

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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability has no effect (it is no longer on the battlefield)."));
            log.info("Game {} - {} ETB has no effect, source left battlefield", gameData.id, entry.getCard().getName());
            return;
        }

        AmountContext ctx = AmountContext.forStackEntry(entry, target);
        int power = amountEvaluationService.evaluate(gameData, effect.power(), ctx);
        int toughness = amountEvaluationService.evaluate(gameData, effect.toughness(), ctx);

        target.setPermanentlyAnimated(true);
        target.setPermanentAnimatedPower(power);
        target.setPermanentAnimatedToughness(toughness);
        addAnimationBasePtFloatingEffect(gameData, entry, target, power, toughness,
                EffectDuration.WHILE_SOURCE_ON_BATTLEFIELD);

        for (CardSubtype subtype : effect.grantedSubtypes()) {
            if (!target.getGrantedSubtypes().contains(subtype)) {
                target.getGrantedSubtypes().add(subtype);
            }
        }

        if (effect.animatedColor() != null) {
            target.getGrantedColors().add(effect.animatedColor());
        }

        gameData.sourceLinkedAnimations.put(target.getId(), sourcePermanentId);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(),
                " becomes a " + power + "/" + toughness + " green Treefolk creature. It's still a land."));

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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            return;
        }
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source.getCard(),
                    "'s ability fizzles — target creature no longer exists."));
            return;
        }

        if (!transformToBackFace(gameData, source)) {
            return;
        }

        gameData.expireFloatingEffectsForUnattachedSource(source.getId());
        source.setAttachedTo(target.getId());
        // CR 613.7e: an attachment receives a new timestamp each time it becomes attached.
        source.setTimestamp(gameData.nextTimestamp());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(source.getCard(), " is attached to ", target.getCard(), "."));
        log.info("Game {} - {} attached to {}", gameData.id, source.getCard().getName(), target.getCard().getName());

        boolean hasControlEffect = source.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ControlEnchantedCreatureEffect);
        if (hasControlEffect) {
            creatureControlService.applyControlEffect(gameData, controllerId, target,
                    new ControlEnchantedCreatureEffect(), EffectDuration.WHILE_ATTACHED,
                    source.getId(), source.getCard().getName());
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

        Card frontCard = self.getCard();
        String frontName = frontCard.getName();
        if (self.isAttached() && !backFace.getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            self.setAttachedTo(null);
            gameData.expireFloatingEffectsForUnattachedSource(self.getId());
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(frontCard, " becomes unattached."));
            log.info("Game {} - {} unattached (transformed into non-Equipment)", gameData.id, frontName);
        }

        self.setCard(backFace);
        self.setTransformed(true);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(frontCard, " transforms into ", backFace, "."));
        log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());

        fireTransformTriggers(gameData, self, backFace, EffectSlot.ON_TRANSFORM_TO_BACK_FACE);
        return true;
    }

    public void transformToFrontFace(GameData gameData, Permanent self) {
        Card originalCard = self.getOriginalCard();
        Card backCard = self.getCard();
        String backName = backCard.getName();
        self.setCard(originalCard);
        self.setTransformed(false);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(backCard, " transforms into ", originalCard, "."));
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(triggerCard, "'s transform ability triggers."));
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(triggerCard,
                        "'s transform ability triggers - choose target opponent."));
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(triggerCard, "'s transform ability triggers."));
                log.info("Game {} - {} transform trigger pushed onto stack", gameData.id, triggerCard.getName());
                return;
            }
        }
    }
}
