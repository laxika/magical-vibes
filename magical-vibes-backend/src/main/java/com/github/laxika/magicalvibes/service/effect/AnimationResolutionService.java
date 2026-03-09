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
import com.github.laxika.magicalvibes.model.effect.AnimateTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        if (target.getAttachedTo() != null && target.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
            target.setAttachedTo(null);
            String unattachLog = target.getCard().getName() + " becomes unattached.";
            gameBroadcastService.logAndBroadcast(gameData, unattachLog);
            log.info("Game {} - {} unattached (equipment became creature)", gameData.id, target.getCard().getName());
        }

        String logEntry = target.getCard().getName() + " becomes a " + effect.power() + "/" + effect.toughness() + " artifact creature.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);

        log.info("Game {} - {} becomes a {}/{} artifact creature permanently", gameData.id, target.getCard().getName(), effect.power(), effect.toughness());
    }
}
