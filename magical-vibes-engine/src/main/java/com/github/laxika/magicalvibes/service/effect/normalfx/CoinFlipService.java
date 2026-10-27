package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.RecordedCoinFlip;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EdgarKingOfFigaroEffect;
import com.github.laxika.magicalvibes.model.effect.KrarksThumbEffect;
import com.github.laxika.magicalvibes.model.effect.PokeyTheScallywaggEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MayEffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
 * <p>Krark's Thumb replaces each flip with two flips, one of which its controller ignores. While the
 * resolving effect's handler is marked
 * {@link com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst} or
 * {@link com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsAtCheckpoints} and has
 * changed nothing since it began (or since its last {@link #checkpoint}), its flips are recorded in
 * {@link GameData#recordedCoinFlips}. When the physical results of such a flip disagree, its player is
 * asked which result to keep, and the handler is stopped with {@link CoinFlipChoicePending} and later
 * resolved again: it reads the recorded flips back with the kept results instead of flipping anew.
 * Each coin-flip call ({@link #flip}, {@link #flipCoins}, {@link #flipEach}) closes that window, and
 * only a checkpoint of the handler's own effect reopens it.</p>
 *
 * <p>A player's answer whose completion flips a coin before changing anything else (a "flip again?" answer, a
 * choice made before a flip) opens the same window with {@link #answerChoosingResultsFirst}; when a flip stops it, the
 * answer is completed again by {@link #completeAgainAfterResultChoices} once every kept result is chosen. A flip made
 * as a permanent enters ({@link #flipAsPermanentEnters}) is asked about before the permanent's enter abilities.
 * Anywhere else the heads result is kept.</p>
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

    @Autowired
    @Lazy
    private MayEffectHandlerRegistry mayEffectHandlerRegistry;

    @Autowired
    @Lazy
    private InputCompletionService inputCompletionService;

    public CoinFlipResult flip(GameData gameData, UUID playerId) {
        return flipCoins(gameData, playerId, 1).getFirst();
    }

    /** Performs a single event that flips {@code count} coins at once. */
    public List<CoinFlipResult> flipCoins(GameData gameData, UUID playerId, int count) {
        return toResults(flipEvent(gameData, playerId, count, true, consumeChoiceWindow(gameData), false));
    }

    /**
     * Has each of {@code playerIds}, in order, flip one coin before anything else happens, so a Krark's
     * Thumb choice on any of these flips can still stop the resolving effect.
     */
    public List<CoinFlipResult> flipEach(GameData gameData, List<UUID> playerIds) {
        boolean choiceWindow = consumeChoiceWindow(gameData);
        List<CoinFlipResult> results = new ArrayList<>(playerIds.size());
        for (UUID playerId : playerIds) {
            results.add(toResult(flipEvent(gameData, playerId, 1, true, choiceWindow, false).getFirst()));
        }
        return results;
    }

    /** Performs coin-flip replacement without allowing Pokey to replace the event again. */
    public CoinFlipResult flipWithoutPokey(GameData gameData, UUID playerId) {
        return toResult(flipEvent(gameData, playerId, 1, false, consumeChoiceWindow(gameData), false).getFirst());
    }

    /**
     * Flips one coin for a replacement effect applied while a permanent enters the battlefield ("as this enters, flip
     * a coin"; Molten Sentry, CR 614.1c). Nothing can be resolved again there, so when the physical results of a
     * Krark's Thumb flip disagree the returned flip still {@linkplain RecordedCoinFlip#awaitsChoice() awaits} its kept
     * result: the flipping player chooses it through {@link #beginEntryResultChoice} before the permanent's enter
     * abilities are processed.
     */
    public RecordedCoinFlip flipAsPermanentEnters(GameData gameData, UUID playerId) {
        consumeChoiceWindow(gameData);
        return flipEvent(gameData, playerId, 1, true, false, true).getFirst();
    }

    /**
     * Asks the player who made {@code flip}, a flip of {@link #flipAsPermanentEnters} still awaiting its kept result,
     * which result to keep; {@code context} carries the entering permanent to the answer.
     */
    public void beginEntryResultChoice(GameData gameData, RecordedCoinFlip flip, ChoiceContext context) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                flip.playerId(), null, null, context,
                List.of(ChoiceContext.CoinFlipResultChoice.HEADS, ChoiceContext.CoinFlipResultChoice.TAILS),
                resultChoicePrompt(flip, "a coin")));
    }

    /** The kept result of {@code flip}, a flip of {@link #flipAsPermanentEnters}, once it is chosen. */
    public CoinFlipResult keptResult(RecordedCoinFlip flip, boolean heads) {
        return toResult(flip.withHeads(heads));
    }

    /**
     * Marks everything the handler resolving {@code effect} did so far as done: if a later flip stops it
     * for a Krark's Thumb choice, it is resolved again from {@code resumePoint} (read with
     * {@link #resumePoint}) and only the flips made after this checkpoint are read back. Does nothing
     * unless {@code effect} is the resolving effect whose handler resumes at checkpoints.
     */
    public void checkpoint(GameData gameData, CardEffect effect, int resumePoint) {
        if (effect == null || gameData.coinFlipCheckpointEffect != effect) {
            return;
        }
        gameData.recordedCoinFlips.clear();
        gameData.replayedCoinFlips = 0;
        gameData.coinFlipResumePoint = resumePoint;
        gameData.coinFlipChoicesCanRerunEffect = true;
    }

    /** Where the handler resolving {@code effect} resumes: its last checkpoint's point, or 0. */
    public int resumePoint(GameData gameData, CardEffect effect) {
        return effect != null && gameData.coinFlipCheckpointEffect == effect ? gameData.coinFlipResumePoint : 0;
    }

    /**
     * Runs {@code completion}, the completion of a player's answer whose first coin-flip call comes before anything
     * else it changes, with the Krark's Thumb choice window open. When a flip of that call stops it, {@code replay} is
     * stored and the answer is completed again through {@link #completeAgainAfterResultChoices} once every kept result
     * is chosen; that run reads the recorded flips back. The window is closed when this returns.
     *
     * @return {@code true} when the completion stopped to ask
     */
    public boolean answerChoosingResultsFirst(GameData gameData, Runnable completion,
                                              GameData.CoinFlipChoiceReplay replay) {
        boolean rerunCurrentEffect = gameData.rerunCurrentEffectAfterInteraction;
        gameData.coinFlipChoicesCanRerunEffect = true;
        gameData.coinFlipCheckpointEffect = null;
        gameData.replayedCoinFlips = 0;
        boolean stopped = false;
        try {
            completion.run();
        } catch (CoinFlipChoicePending pending) {
            stopped = true;
            // A parked stack resolution stays as it is; only this answer is completed again.
            gameData.rerunCurrentEffectAfterInteraction = rerunCurrentEffect;
            gameData.pendingCoinFlipChoiceReplay = replay;
            inputCompletionService.publishStateAfterInput(gameData);
        } finally {
            gameData.coinFlipChoicesCanRerunEffect = false;
            if (!stopped) {
                gameData.recordedCoinFlips.clear();
                gameData.replayedCoinFlips = 0;
            }
        }
        return stopped;
    }

    /**
     * Completes the answer stopped by {@link #answerChoosingResultsFirst} again, once every Krark's Thumb result it
     * asked about is chosen.
     *
     * @return {@code false} when no answer is waiting
     */
    public boolean completeAgainAfterResultChoices(GameData gameData) {
        GameData.CoinFlipChoiceReplay replay = gameData.pendingCoinFlipChoiceReplay;
        if (replay == null) {
            return false;
        }
        gameData.pendingCoinFlipChoiceReplay = null;
        switch (replay) {
            case GameData.MayAnswerReplay answerReplay ->
                    mayEffectHandlerRegistry.getHandler(answerReplay.ability().effects().getFirst())
                            .handle(gameData, answerReplay.player(), answerReplay.accepted(), answerReplay.ability());
            case GameData.InteractionAnswerReplay answerReplay -> {
                gameData.interaction.beginInteraction(answerReplay.interaction());
                interactionHandlerRegistry.dispatchAnswer(gameData, answerReplay.player(),
                        (InteractionAnswer) answerReplay.answer());
            }
        }
        return true;
    }

    /**
     * {@code true} while the completion of an answer is completed again after its Krark's Thumb choices and has not
     * yet read back all its recorded flips, so a change it made before its first flip (a log line) is not repeated.
     */
    public boolean replaysRecordedFlips(GameData gameData) {
        return gameData.coinFlipChoicesCanRerunEffect
                && gameData.replayedCoinFlips < gameData.recordedCoinFlips.size();
    }

    private static boolean consumeChoiceWindow(GameData gameData) {
        boolean choiceWindow = gameData.coinFlipChoicesCanRerunEffect;
        gameData.coinFlipChoicesCanRerunEffect = false;
        return choiceWindow;
    }

    /**
     * @param leaveChoiceOpen return a Krark's Thumb flip whose physical results disagree as still awaiting its kept
     *                        result, for a caller that asks it later; otherwise such a flip stops the effect inside
     *                        an open choice window, or keeps heads outside one
     */
    private List<RecordedCoinFlip> flipEvent(GameData gameData, UUID playerId, int count,
                                             boolean applyPokey, boolean choiceWindow, boolean leaveChoiceOpen) {
        if (choiceWindow && gameData.replayedCoinFlips < gameData.recordedCoinFlips.size()) {
            return replayRecordedFlips(gameData, count);
        }

        boolean pokeyApplies = applyPokey && gameQueryService.countPlayerControlledStaticEffects(
                gameData, playerId, PokeyTheScallywaggEffect.class) > 0;
        if (pokeyApplies) {
            List<RecordedCoinFlip> rolls = new ArrayList<>(count);
            for (int coin = 0; coin < count; coin++) {
                int roll = ThreadLocalRandom.current().nextInt(1, 21);
                rolls.add(new RecordedCoinFlip(playerId, roll >= 11, 1, roll >= 11 ? 1 : 0, false, roll));
            }
            if (choiceWindow) {
                record(gameData, rolls);
            }
            if (triggerCollectionService != null) {
                triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                        gameData, playerId, count, rolls.getLast().pokeyD20Result());
            }
            return rolls;
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
            flips.add(new RecordedCoinFlip(playerId, heads, physicalFlips, physicalHeads, edgarApplies, null));
        }

        // The effect can stop only while no other question of it is waiting for an answer.
        boolean choiceCanStopEffect = choiceWindow && interactionHandlerRegistry != null
                && !gameData.interaction.isAwaitingInput();
        if (!choiceCanStopEffect && !leaveChoiceOpen) {
            // No resumable choice on this path: keep a heads result if any physical flip came up heads.
            flips.replaceAll(flip -> flip.awaitsChoice() ? flip.withHeads(true) : flip);
        }
        if (choiceWindow) {
            record(gameData, flips);
        }
        if (choiceCanStopEffect && flips.stream().anyMatch(RecordedCoinFlip::awaitsChoice)) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            beginNextResultChoice(gameData);
            throw new CoinFlipChoicePending();
        }
        return flips;
    }

    private static List<CoinFlipResult> toResults(List<RecordedCoinFlip> flips) {
        return flips.stream().map(CoinFlipService::toResult).toList();
    }

    private static void record(GameData gameData, List<RecordedCoinFlip> flips) {
        gameData.recordedCoinFlips.addAll(flips);
        gameData.replayedCoinFlips += flips.size();
    }

    private List<RecordedCoinFlip> replayRecordedFlips(GameData gameData, int count) {
        int from = gameData.replayedCoinFlips;
        int to = Math.min(from + count, gameData.recordedCoinFlips.size());
        gameData.replayedCoinFlips = to;
        return gameData.recordedCoinFlips.subList(from, to).stream()
                .map(flip -> flip.awaitsChoice() ? flip.withHeads(true) : flip)
                .toList();
    }

    private static CoinFlipResult toResult(RecordedCoinFlip flip) {
        return new CoinFlipResult(flip.heads(), flip.physicalFlips(), flip.edgarApplied(), flip.pokeyD20Result());
    }

    /**
     * Asks the flipping player which result to keep for the first recorded flip still awaiting that choice.
     *
     * @return {@code false} when every recorded flip already has its kept result
     */
    public boolean beginNextResultChoice(GameData gameData) {
        List<RecordedCoinFlip> flips = gameData.recordedCoinFlips;
        for (int index = 0; index < flips.size(); index++) {
            RecordedCoinFlip flip = flips.get(index);
            if (!flip.awaitsChoice()) {
                continue;
            }
            List<RecordedCoinFlip> playerFlips = flips.stream()
                    .filter(other -> other.playerId().equals(flip.playerId()))
                    .toList();
            String coin = playerFlips.size() == 1 ? "a coin"
                    : "coin " + (playerFlips.indexOf(flip) + 1) + " of " + playerFlips.size();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    flip.playerId(), null, null,
                    new ChoiceContext.CoinFlipResultChoice(flip.playerId(), index),
                    List.of(ChoiceContext.CoinFlipResultChoice.HEADS, ChoiceContext.CoinFlipResultChoice.TAILS),
                    resultChoicePrompt(flip, coin)));
            return true;
        }
        return false;
    }

    private static String resultChoicePrompt(RecordedCoinFlip flip, String coin) {
        int tails = flip.physicalFlips() - flip.physicalHeads();
        return "Krark's Thumb: flipping " + coin + " came up " + flip.physicalHeads() + " heads and "
                + tails + " tails. Choose the result to keep; the others are ignored.";
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
