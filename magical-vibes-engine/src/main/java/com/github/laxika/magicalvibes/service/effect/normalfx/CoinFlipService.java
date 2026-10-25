package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.RecordedCoinFlip;
import com.github.laxika.magicalvibes.model.effect.EdgarKingOfFigaroEffect;
import com.github.laxika.magicalvibes.model.effect.KrarksThumbEffect;
import com.github.laxika.magicalvibes.model.effect.PokeyTheScallywaggEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Performs one logical coin flip, including coin-flip replacement effects controlled by its player.
 *
 * <p>Krark's Thumb replaces each flip with two flips, one of which its controller ignores. When the
 * physical results disagree and the flip is the first coin-flip event of a handler marked
 * {@link com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst}, the flips are
 * recorded in {@link GameData#recordedCoinFlips}, the player is asked which result to keep for each
 * such coin, and the handler is stopped with {@link CoinFlipChoicePending} and later resolved again
 * with the kept results. Anywhere else the heads result is kept.</p>
 */
@Component
@RequiredArgsConstructor
public class CoinFlipService {

    private final GameQueryService gameQueryService;

    @Autowired
    @Lazy
    private TriggerCollectionService triggerCollectionService;

    @Autowired
    @Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;

    public CoinFlipResult flip(GameData gameData, UUID playerId) {
        return flipCoins(gameData, playerId, 1).getFirst();
    }

    /** Performs a single event that flips {@code count} coins at once. */
    public List<CoinFlipResult> flipCoins(GameData gameData, UUID playerId, int count) {
        return flipCoins(gameData, playerId, count, true);
    }

    /** Performs coin-flip replacement without allowing Pokey to replace the event again. */
    public CoinFlipResult flipWithoutPokey(GameData gameData, UUID playerId) {
        return flipCoins(gameData, playerId, 1, false).getFirst();
    }

    private List<CoinFlipResult> flipCoins(GameData gameData, UUID playerId, int count,
                                           boolean applyPokey) {
        // Only a marked handler's first coin-flip event may stop it for a Krark's Thumb choice.
        boolean choiceCanRerunEffect = gameData.coinFlipChoicesCanRerunEffect;
        gameData.coinFlipChoicesCanRerunEffect = false;
        if (choiceCanRerunEffect && !gameData.recordedCoinFlips.isEmpty()) {
            return replayRecordedFlips(gameData);
        }

        boolean pokeyApplies = applyPokey && gameQueryService.countPlayerControlledStaticEffects(
                gameData, playerId, PokeyTheScallywaggEffect.class) > 0;
        if (pokeyApplies) {
            List<CoinFlipResult> results = new ArrayList<>(count);
            for (int coin = 0; coin < count; coin++) {
                int roll = ThreadLocalRandom.current().nextInt(1, 21);
                results.add(new CoinFlipResult(roll >= 11, 1, false, roll));
            }
            if (triggerCollectionService != null) {
                triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                        gameData, playerId, count, results.getLast().pokeyD20Result());
            }
            return results;
        }

        int thumbCount = gameQueryService.countPlayerControlledStaticEffects(
                gameData, playerId, KrarksThumbEffect.class);
        int physicalFlips = 1 << thumbCount;
        boolean edgarApplies = gameData.playersWhoFlippedCoinsThisTurn.add(playerId)
                && gameQueryService.countPlayerControlledStaticEffects(
                        gameData, playerId, EdgarKingOfFigaroEffect.class) > 0;

        // Every coin of the event is flipped before any result is chosen, so the player knows all of them.
        List<RecordedCoinFlip> flips = new ArrayList<>(count);
        for (int coin = 0; coin < count; coin++) {
            int physicalHeads = physicalFlips;
            if (!edgarApplies) {
                physicalHeads = 0;
                for (int i = 0; i < physicalFlips; i++) {
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        physicalHeads++;
                    }
                }
            }
            Boolean heads = physicalHeads == physicalFlips ? Boolean.TRUE
                    : physicalHeads == 0 ? Boolean.FALSE
                    : null;
            flips.add(new RecordedCoinFlip(heads, physicalFlips, physicalHeads, edgarApplies));
        }

        if (flips.stream().anyMatch(RecordedCoinFlip::awaitsChoice)) {
            if (choiceCanRerunEffect && interactionHandlerRegistry != null) {
                gameData.recordedCoinFlips.addAll(flips);
                gameData.rerunCurrentEffectAfterInteraction = true;
                beginNextResultChoice(gameData, playerId);
                throw new CoinFlipChoicePending();
            }
            // No resumable choice on this path: keep a heads result if any physical flip came up heads.
            flips.replaceAll(flip -> flip.awaitsChoice() ? flip.withHeads(true) : flip);
        }
        return flips.stream().map(CoinFlipService::toResult).toList();
    }

    private List<CoinFlipResult> replayRecordedFlips(GameData gameData) {
        List<CoinFlipResult> results = gameData.recordedCoinFlips.stream()
                .map(flip -> flip.awaitsChoice() ? flip.withHeads(true) : flip)
                .map(CoinFlipService::toResult)
                .toList();
        gameData.recordedCoinFlips.clear();
        return results;
    }

    private static CoinFlipResult toResult(RecordedCoinFlip flip) {
        return new CoinFlipResult(flip.heads(), flip.physicalFlips(), flip.edgarApplied(), null);
    }

    /**
     * Asks {@code playerId} which result to keep for the first recorded flip still awaiting that choice.
     *
     * @return {@code false} when every recorded flip already has its kept result
     */
    public boolean beginNextResultChoice(GameData gameData, UUID playerId) {
        List<RecordedCoinFlip> flips = gameData.recordedCoinFlips;
        for (int index = 0; index < flips.size(); index++) {
            RecordedCoinFlip flip = flips.get(index);
            if (!flip.awaitsChoice()) {
                continue;
            }
            int tails = flip.physicalFlips() - flip.physicalHeads();
            String coin = flips.size() == 1 ? "a coin" : "coin " + (index + 1) + " of " + flips.size();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    playerId, null, null,
                    new ChoiceContext.CoinFlipResultChoice(playerId, index),
                    List.of(ChoiceContext.CoinFlipResultChoice.HEADS, ChoiceContext.CoinFlipResultChoice.TAILS),
                    "Krark's Thumb: flipping " + coin + " came up " + flip.physicalHeads() + " heads and "
                            + tails + " tails. Choose the result to keep; the others are ignored."));
            return true;
        }
        return false;
    }

    /** Records the result {@code playerId} keeps for the recorded flip at {@code coinIndex}. */
    public void chooseResult(GameData gameData, int coinIndex, boolean heads) {
        RecordedCoinFlip flip = gameData.recordedCoinFlips.get(coinIndex);
        if (!flip.awaitsChoice()) {
            throw new IllegalStateException("That coin flip's result was already chosen");
        }
        gameData.recordedCoinFlips.set(coinIndex, flip.withHeads(heads));
    }

    public String replacementDetails(CoinFlipResult result) {
        if (result.pokeyD20Result() != null) {
            return " (Pokey, the Scallywagg rolled a d20: " + result.pokeyD20Result() + ")";
        }
        if (result.edgarApplied()) {
            return result.physicalFlips() == 1
                    ? " (Edgar, King of Figaro made it come up heads)"
                    : " (Edgar, King of Figaro made all physical flips come up heads; flipped "
                            + result.physicalFlips() + ")";
        }
        if (result.physicalFlips() == 1) {
            return "";
        }
        return " (flipped " + result.physicalFlips() + " coins and ignored "
                + (result.physicalFlips() - 1) + ")";
    }

    public String replacementDetailsForCoins(List<CoinFlipResult> results) {
        if (results.stream().anyMatch(result -> result.pokeyD20Result() != null)) {
            String rolls = results.stream()
                    .map(CoinFlipResult::pokeyD20Result)
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(", "));
            return " (Pokey, the Scallywagg rolled d20s: " + rolls + ")";
        }
        int physicalFlips = results.stream().mapToInt(CoinFlipResult::physicalFlips).sum();
        boolean edgarApplied = results.stream().anyMatch(CoinFlipResult::edgarApplied);
        if (edgarApplied) {
            return physicalFlips == results.size()
                    ? " (Edgar, King of Figaro made all of these flips come up heads)"
                    : " (Edgar, King of Figaro made all physical flips come up heads; flipped "
                            + physicalFlips + ")";
        }
        if (physicalFlips == results.size()) {
            return "";
        }
        return " (" + physicalFlips + " physical coin flips; one result kept per coin)";
    }

    public record CoinFlipResult(boolean heads, int physicalFlips, boolean edgarApplied,
                                 Integer pokeyD20Result) {
        public CoinFlipResult(boolean heads, int physicalFlips, boolean edgarApplied) {
            this(heads, physicalFlips, edgarApplied, null);
        }

        public CoinFlipResult(boolean heads, int physicalFlips) {
            this(heads, physicalFlips, false, null);
        }

        public boolean isActualCoinFlip() {
            return pokeyD20Result == null;
        }
    }
}
