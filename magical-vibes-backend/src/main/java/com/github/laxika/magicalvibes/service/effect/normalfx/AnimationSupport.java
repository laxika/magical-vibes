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
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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

    public void animateSingleLand(GameData gameData, StackEntry entry, AnimateLandEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (self == null) {
            return;
        }

        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(effect.power());
        self.setAnimatedToughness(effect.toughness());
        self.setAnimatedColor(effect.animatedColor());
        self.getTransientSubtypes().clear();
        self.getTransientSubtypes().addAll(effect.grantedSubtypes());
        self.getGrantedKeywords().addAll(effect.grantedKeywords());
        self.getGrantedCardTypes().addAll(effect.grantedCardTypes());

        String logEntry = self.getCard().getName() + " becomes a " + effect.power() + "/" + effect.toughness() + " creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), effect.power(), effect.toughness());
    }

    public void animateOwnLands(GameData gameData, StackEntry entry, AnimateLandEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        boolean untilNextTurn = effect.duration() == EffectDuration.UNTIL_YOUR_NEXT_TURN;

        for (Permanent perm : battlefield) {
            if (!perm.getCard().hasType(CardType.LAND)) {
                continue;
            }

            if (untilNextTurn) {
                perm.setAnimatedUntilNextTurn(true);
                perm.setUntilNextTurnAnimatedPower(effect.power());
                perm.setUntilNextTurnAnimatedToughness(effect.toughness());
                perm.getUntilNextTurnSubtypes().clear();
                perm.getUntilNextTurnSubtypes().addAll(effect.grantedSubtypes());
                perm.getUntilNextTurnKeywords().addAll(effect.grantedKeywords());
            } else {
                perm.setAnimatedUntilEndOfTurn(true);
                perm.setAnimatedPower(effect.power());
                perm.setAnimatedToughness(effect.toughness());
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
                "All lands you control become " + effect.power() + "/" + effect.toughness()
                        + " Elemental creatures with reach, indestructible, and haste " + durationText + ". They're still lands.");
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
