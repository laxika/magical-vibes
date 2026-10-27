package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.RecordedCoinFlip;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.FlipCoinOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * "As this enters, flip a coin" (Molten Sentry, CR 614.1c): the result fixes the entering permanent's base power,
 * toughness and keywords. When Krark's Thumb's physical flips disagree, the flip is left on the permanent
 * ({@link Permanent#getEntryCoinFlipAwaitingChoice()}) and its controller chooses the kept result through
 * {@link #beginPendingResultChoice} before the permanent's enter abilities are processed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinOnEnterEffectHandler implements EntryReplacementHandlerBean {

    private final CoinFlipService coinFlipService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinOnEnterEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect) {
        RecordedCoinFlip flip = coinFlipService.flipAsPermanentEnters(gameData, controllerId);
        if (flip.awaitsChoice()) {
            enteringPermanent.setEntryCoinFlipAwaitingChoice(flip);
            return;
        }
        applyResult(gameData, controllerId, enteringPermanent, (FlipCoinOnEnterEffect) effect,
                coinFlipService.keptResult(flip, flip.heads()));
    }

    /**
     * Asks the controller of {@code permanent} which result to keep of its entry flip when one still awaits that
     * choice, unless another question is already waiting for an answer: then the heads result is kept.
     *
     * @return {@code true} when the choice was begun; {@code context} resumes the permanent's enter abilities
     */
    public boolean beginPendingResultChoice(GameData gameData, Permanent permanent,
                                            ChoiceContext.EntryCoinFlipResultChoice context) {
        RecordedCoinFlip flip = permanent.getEntryCoinFlipAwaitingChoice();
        if (flip == null) {
            return false;
        }
        if (gameData.interaction.isAwaitingInput()) {
            keepResult(gameData, context.controllerId(), permanent, true);
            return false;
        }
        coinFlipService.beginEntryResultChoice(gameData, flip, context);
        return true;
    }

    /** Fixes the form of {@code permanent} with the kept result of its entry flip that awaited the choice. */
    public void keepResult(GameData gameData, UUID controllerId, Permanent permanent, boolean heads) {
        RecordedCoinFlip flip = permanent.getEntryCoinFlipAwaitingChoice();
        if (flip == null) {
            return;
        }
        permanent.setEntryCoinFlipAwaitingChoice(null);
        permanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(FlipCoinOnEnterEffect.class::isInstance)
                .map(FlipCoinOnEnterEffect.class::cast)
                .findFirst()
                .ifPresent(flipEffect -> applyResult(gameData, controllerId, permanent, flipEffect,
                        coinFlipService.keptResult(flip, heads)));
    }

    private void applyResult(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                             FlipCoinOnEnterEffect flipEffect, CoinFlipService.CoinFlipResult result) {
        boolean heads = result.heads();
        String sourceName = enteringPermanent.getCard().getName();

        String flipLog = heads
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + ".";
        gameLogService.append(gameData, GameLog.text(flipLog));
        enteringPermanent.setEntryCoinFlipHeads(heads);
        int power = heads ? flipEffect.headsPower() : flipEffect.tailsPower();
        int toughness = heads ? flipEffect.headsToughness() : flipEffect.tailsToughness();
        Set<Keyword> keywords = heads ? flipEffect.headsKeywords() : flipEffect.tailsKeywords();
        SetBasePowerToughnessEffect setter = new SetBasePowerToughnessEffect(
                power, toughness, GrantScope.SELF, EffectDuration.PERMANENT);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), sourceName, enteringPermanent.getId(), controllerId, setter,
                enteringPermanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        enteringPermanent.getPersistentGrantedKeywords().addAll(keywords);

        log.info("Game {} - {} enters with base power and toughness {}/{}", gameData.id,
                sourceName, power, toughness);
    }
}
