package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.RecordedCoinFlip;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinOnEnterEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.CoinFlipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * "As this enters, flip a coin" (Molten Sentry, CR 614.1c): the result fixes the entering permanent's power,
 * toughness and keywords as copiable values (CR 707.2). When Krark's Thumb's physical flips disagree, the flip is left on the permanent
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
        permanent.getCard().getStaticEffects().stream()
                .filter(FlipCoinOnEnterEffect.class::isInstance)
                .map(FlipCoinOnEnterEffect.class::cast)
                .findFirst()
                .ifPresent(flipEffect -> applyResult(gameData, controllerId, permanent, flipEffect,
                        coinFlipService.keptResult(flip, heads)));
    }

    private void applyResult(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                             FlipCoinOnEnterEffect flipEffect, CoinFlipService.CoinFlipResult result) {
        boolean heads = result.heads();
        String sourceName = FaceDownVisibility.publicName(gameData, enteringPermanent);

        // A flip that cares only whether the coin comes up heads or tails has no winner (CR 705.2).
        String flipLog = gameData.playerIdToName.get(controllerId) + "'s coin flip for " + sourceName
                + " comes up " + (heads ? "heads" : "tails") + coinFlipService.replacementDetails(result) + ".";
        gameLogService.append(gameData, GameLog.text(flipLog));
        int power = heads ? flipEffect.headsPower() : flipEffect.tailsPower();
        int toughness = heads ? flipEffect.headsToughness() : flipEffect.tailsToughness();
        // An as-enters ability that sets power and toughness: the form is part of the permanent's copiable values
        // (CR 707.2), applied in layer 1 (CR 613.1a), so a permanent that becomes a copy of it takes the form without
        // flipping.
        Card form = enteringPermanent.getCard().createRuntimeCopy();
        form.setPower(power);
        form.setToughness(toughness);
        Set<Keyword> keywords = new LinkedHashSet<>(form.getKeywords());
        keywords.addAll(heads ? flipEffect.headsKeywords() : flipEffect.tailsKeywords());
        form.setKeywords(keywords);
        enteringPermanent.setCard(form);

        log.info("Game {} - {} enters with base power and toughness {}/{}", gameData.id,
                sourceName, power, toughness);
    }
}
