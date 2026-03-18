package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddCardTypeToTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfByChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetLandWhileSourceOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TapAndTransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TransformAllEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnimationResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @HandlesEffect(AnimateLandEffect.class)
    private void resolveAnimateLand(GameData gameData, StackEntry entry, AnimateLandEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
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

    @HandlesEffect(AnimateSelfEffect.class)
    private void resolveAnimateSelf(GameData gameData, StackEntry entry, AnimateSelfEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        int xValue = entry.getXValue();
        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(xValue);
        self.setAnimatedToughness(xValue);
        self.getTransientSubtypes().clear();
        self.getTransientSubtypes().addAll(effect.grantedSubtypes());

        String logEntry = self.getCard().getName() + " becomes a " + xValue + "/" + xValue + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), xValue, xValue);
    }

    @HandlesEffect(AnimateSelfByChargeCountersEffect.class)
    private void resolveAnimateSelfByChargeCounters(GameData gameData, StackEntry entry, AnimateSelfByChargeCountersEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        int counters = self.getChargeCounters();
        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(counters);
        self.setAnimatedToughness(counters);
        self.getTransientSubtypes().clear();
        self.getTransientSubtypes().addAll(effect.grantedSubtypes());

        String logEntry = self.getCard().getName() + " becomes a " + counters + "/" + counters + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature", gameData.id, self.getCard().getName(), counters, counters);
    }

    @HandlesEffect(AnimateSelfWithStatsEffect.class)
    private void resolveAnimateSelfWithStats(GameData gameData, StackEntry entry, AnimateSelfWithStatsEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (self == null) {
            return;
        }

        self.setAnimatedUntilEndOfTurn(true);
        self.setAnimatedPower(effect.power());
        self.setAnimatedToughness(effect.toughness());
        self.getTransientSubtypes().clear();
        self.getTransientSubtypes().addAll(effect.grantedSubtypes());
        self.getGrantedKeywords().addAll(effect.grantedKeywords());

        String logEntry = self.getCard().getName() + " becomes a " + effect.power() + "/" + effect.toughness()
                + " artifact creature until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature with {}",
                gameData.id, self.getCard().getName(), effect.power(), effect.toughness(), effect.grantedKeywords());
    }

    @HandlesEffect(AddCardTypeToTargetPermanentEffect.class)
    private void resolveAddCardTypeToTargetPermanent(GameData gameData, StackEntry entry, AddCardTypeToTargetPermanentEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.getGrantedCardTypes().add(effect.cardType());

        String typeName = effect.cardType().name().charAt(0) + effect.cardType().name().substring(1).toLowerCase();
        String logEntry = target.getCard().getName() + " becomes an " + typeName + " in addition to its other types until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes an {} until end of turn", gameData.id, target.getCard().getName(), typeName);
    }

    @HandlesEffect(AnimateTargetPermanentEffect.class)
    private void resolveAnimateTargetPermanent(GameData gameData, StackEntry entry, AnimateTargetPermanentEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        target.setPermanentlyAnimated(true);
        target.setPermanentAnimatedPower(effect.power());
        target.setPermanentAnimatedToughness(effect.toughness());

        // Per MTG rules: if an Equipment becomes a creature, it becomes unattached (CR 301.5c)
        if (target.isAttached() && target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            target.setAttachedTo(null);
            String unattachLog = target.getCard().getName() + " becomes unattached.";
            gameBroadcastService.logAndBroadcast(gameData, unattachLog);
            log.info("Game {} - {} unattached (equipment became creature)", gameData.id, target.getCard().getName());
        }

        String logEntry = target.getCard().getName() + " becomes a " + effect.power() + "/" + effect.toughness() + " artifact creature.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} artifact creature permanently", gameData.id, target.getCard().getName(), effect.power(), effect.toughness());
    }

    @HandlesEffect(TransformSelfEffect.class)
    private void resolveTransformSelf(GameData gameData, StackEntry entry, TransformSelfEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        Card originalCard = self.getOriginalCard();
        if (!self.isTransformed()) {
            // Transform to back face
            Card backFace = originalCard.getBackFaceCard();
            if (backFace == null) {
                log.warn("Game {} - {} has no back face to transform to", gameData.id, self.getCard().getName());
                return;
            }
            String frontName = self.getCard().getName();
            self.setCard(backFace);
            self.setTransformed(true);
            String logEntry = frontName + " transforms into " + backFace.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
        } else {
            // Transform back to front face
            String backName = self.getCard().getName();
            self.setCard(originalCard);
            self.setTransformed(false);
            String logEntry = backName + " transforms into " + originalCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
        }
    }

    @HandlesEffect(TransformAllEffect.class)
    private void resolveTransformAll(GameData gameData, StackEntry entry, TransformAllEffect effect) {
        gameData.forEachPermanent((playerId, perm) -> {
            if (!gameQueryService.matchesPermanentPredicate(gameData, perm, effect.filter())) {
                return;
            }
            Card originalCard = perm.getOriginalCard();
            if (!perm.isTransformed()) {
                Card backFace = originalCard.getBackFaceCard();
                if (backFace == null) {
                    return;
                }
                String frontName = perm.getCard().getName();
                perm.setCard(backFace);
                perm.setTransformed(true);
                String logEntry = frontName + " transforms into " + backFace.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
            } else {
                String backName = perm.getCard().getName();
                perm.setCard(originalCard);
                perm.setTransformed(false);
                String logEntry = backName + " transforms into " + originalCard.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
            }
        });
    }

    @HandlesEffect(TapAndTransformSelfEffect.class)
    private void resolveTapAndTransformSelf(GameData gameData, StackEntry entry, TapAndTransformSelfEffect effect) {
        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        // Tap
        self.tap();
        String tapLog = self.getCard().getName() + " is tapped.";
        gameBroadcastService.logAndBroadcast(gameData, tapLog);
        log.info("Game {} - {} is tapped", gameData.id, self.getCard().getName());

        // Transform
        Card originalCard = self.getOriginalCard();
        if (self.isTransformed()) {
            String backName = self.getCard().getName();
            self.setCard(originalCard);
            self.setTransformed(false);
            String logEntry = backName + " transforms into " + originalCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
        } else {
            Card backFace = originalCard.getBackFaceCard();
            if (backFace == null) {
                log.warn("Game {} - {} has no back face to transform to", gameData.id, self.getCard().getName());
                return;
            }
            String frontName = self.getCard().getName();
            self.setCard(backFace);
            self.setTransformed(true);
            String logEntry = frontName + " transforms into " + backFace.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
        }
    }

    @HandlesEffect(AnimateTargetLandWhileSourceOnBattlefieldEffect.class)
    private void resolveAnimateTargetLandWhileSourceOnBattlefield(GameData gameData, StackEntry entry,
                                                                   AnimateTargetLandWhileSourceOnBattlefieldEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
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

        target.setPermanentlyAnimated(true);
        target.setPermanentAnimatedPower(effect.power());
        target.setPermanentAnimatedToughness(effect.toughness());

        for (CardSubtype subtype : effect.grantedSubtypes()) {
            if (!target.getGrantedSubtypes().contains(subtype)) {
                target.getGrantedSubtypes().add(subtype);
            }
        }

        if (effect.color() != null) {
            target.getGrantedColors().add(effect.color());
        }

        gameData.sourceLinkedAnimations.put(target.getId(), sourcePermanentId);

        String logEntry = target.getCard().getName() + " becomes a " + effect.power() + "/" + effect.toughness()
                + " green Treefolk creature. It's still a land.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} creature while {} is on the battlefield",
                gameData.id, target.getCard().getName(), effect.power(), effect.toughness(),
                entry.getCard().getName());
    }
}
