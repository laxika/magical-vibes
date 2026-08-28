package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiPermanentChoiceAiStrategyTest {

    private final MultiPermanentChoiceAiStrategy strategy = new MultiPermanentChoiceAiStrategy();

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private AiGameActions gameActions;

    private GameData gameData;
    private UUID aiPlayerId;
    private UUID opponentId;

    @BeforeEach
    void setUp() {
        aiPlayerId = UUID.randomUUID();
        opponentId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", aiPlayerId, "AI");
        gameData.orderedPlayerIds.add(aiPlayerId);
        gameData.orderedPlayerIds.add(opponentId);
        gameData.playerBattlefields.put(aiPlayerId, Collections.synchronizedList(new ArrayList<>()));
        gameData.playerBattlefields.put(opponentId, Collections.synchronizedList(new ArrayList<>()));
    }

    @Test
    @DisplayName("handledType is MultiPermanentChoice")
    void handledType() {
        assertThat(strategy.handledType()).isEqualTo(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    @DisplayName("Registered in AiInteractionStrategies")
    void registeredInStrategies() {
        var interaction = multiChoice(aiPlayerId, List.of(UUID.randomUUID()), 1);
        assertThat(AiInteractionStrategies.forInteraction(interaction))
                .isInstanceOf(MultiPermanentChoiceAiStrategy.class);
    }

    @Test
    @DisplayName("Wrong deciding player: does not answer")
    void ignoresWrongPlayer() throws Exception {
        strategy.answer(multiChoice(opponentId, List.of(UUID.randomUUID()), 1), context());

        verify(gameActions, never()).answerInteraction(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("No valid permanents or players: does not answer")
    void ignoresChoiceWithNoValidTargets() throws Exception {
        strategy.answer(multiChoice(aiPlayerId, List.of(), 2), context());

        verify(gameActions, never()).answerInteraction(
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Player-only choice: selects valid players up to maxCount")
    void selectsValidPlayersWhenNoPermanentsAreValid() throws Exception {
        strategy.answer(multiChoice(
                aiPlayerId, List.of(), List.of(opponentId, aiPlayerId), 2), context());

        assertChosen(List.of(opponentId, aiPlayerId));
    }

    @Test
    @DisplayName("Prefers opponent's strongest permanents by effective power up to maxCount")
    void prefersOpponentStrongestUpToMaxCount() throws Exception {
        Permanent weak = creature("Weak", 1);
        Permanent mid = creature("Mid", 3);
        Permanent strong = creature("Strong", 5);
        gameData.playerBattlefields.get(opponentId).addAll(List.of(weak, mid, strong));

        when(gameQueryService.getEffectivePower(gameData, weak)).thenReturn(1);
        when(gameQueryService.getEffectivePower(gameData, mid)).thenReturn(3);
        when(gameQueryService.getEffectivePower(gameData, strong)).thenReturn(5);

        strategy.answer(multiChoice(aiPlayerId,
                List.of(weak.getId(), mid.getId(), strong.getId()), 2), context());

        assertChosen(List.of(strong.getId(), mid.getId()));
    }

    @Test
    @DisplayName("Respects maxCount of 1 among opponent permanents")
    void respectsMaxCountOne() throws Exception {
        Permanent weak = creature("Weak", 1);
        Permanent strong = creature("Strong", 5);
        gameData.playerBattlefields.get(opponentId).addAll(List.of(weak, strong));

        when(gameQueryService.getEffectivePower(gameData, weak)).thenReturn(1);
        when(gameQueryService.getEffectivePower(gameData, strong)).thenReturn(5);

        strategy.answer(multiChoice(aiPlayerId, List.of(weak.getId(), strong.getId()), 1), context());

        assertChosen(List.of(strong.getId()));
    }

    @Test
    @DisplayName("Ignores opponent permanents that are not in the valid id list")
    void ignoresInvalidOpponentPermanents() throws Exception {
        Permanent valid = creature("Valid", 2);
        Permanent invalid = creature("Invalid", 9);
        gameData.playerBattlefields.get(opponentId).addAll(List.of(valid, invalid));

        strategy.answer(multiChoice(aiPlayerId, List.of(valid.getId()), 2), context());

        assertChosen(List.of(valid.getId()));
    }

    @Test
    @DisplayName("Falls back to first valid ids when no opponent permanent matches")
    void fallsBackToFirstValidIds() throws Exception {
        Permanent own = creature("Own", 1);
        gameData.playerBattlefields.get(aiPlayerId).add(own);
        UUID orphanA = UUID.randomUUID();
        UUID orphanB = UUID.randomUUID();
        UUID orphanC = UUID.randomUUID();

        strategy.answer(multiChoice(aiPlayerId, List.of(orphanA, orphanB, orphanC), 2), context());

        assertChosen(List.of(orphanA, orphanB));
    }

    @Test
    @DisplayName("Falls back includes own permanents when they are the only valid ids")
    void fallsBackToOwnValidPermanents() throws Exception {
        Permanent a = creature("A", 1);
        Permanent b = creature("B", 2);
        gameData.playerBattlefields.get(aiPlayerId).addAll(List.of(a, b));

        strategy.answer(multiChoice(aiPlayerId, List.of(a.getId(), b.getId()), 2), context());

        assertChosen(List.of(a.getId(), b.getId()));
    }

    @Test
    @DisplayName("Power-limited keep choice does not exceed the effective power limit")
    void powerLimitedKeepChoiceStaysWithinLimit() throws Exception {
        Permanent strongestLegal = creature("Strongest legal", 4);
        Permanent extra = creature("Extra", 1);
        gameData.playerBattlefields.get(aiPlayerId).addAll(List.of(strongestLegal, extra));

        when(gameQueryService.getEffectivePower(gameData, strongestLegal)).thenReturn(4);
        when(gameQueryService.getEffectivePower(gameData, extra)).thenReturn(1);

        strategy.answer(powerLimitedChoice(
                List.of(strongestLegal.getId(), extra.getId()), 4), context());

        assertChosen(List.of(strongestLegal.getId()));
    }

    @Test
    @DisplayName("Power-limited keep choice may keep no creatures")
    void powerLimitedKeepChoiceCanBeEmpty() throws Exception {
        Permanent tooLarge = creature("Too large", 5);
        gameData.playerBattlefields.get(aiPlayerId).add(tooLarge);

        when(gameQueryService.getEffectivePower(gameData, tooLarge)).thenReturn(5);

        strategy.answer(powerLimitedChoice(List.of(tooLarge.getId()), 4), context());

        assertChosen(List.of());
    }

    @Test
    @DisplayName("Negative effective power expands a power-limited keep choice")
    void negativePowerExpandsPowerLimitedKeepChoice() throws Exception {
        Permanent negative = creature("Negative", -1);
        Permanent fivePower = creature("Five power", 5);
        gameData.playerBattlefields.get(aiPlayerId).addAll(List.of(negative, fivePower));

        when(gameQueryService.getEffectivePower(gameData, negative)).thenReturn(-1);
        when(gameQueryService.getEffectivePower(gameData, fivePower)).thenReturn(5);

        strategy.answer(powerLimitedChoice(
                List.of(negative.getId(), fivePower.getId()), 4), context());

        assertChosen(List.of(negative.getId(), fivePower.getId()));
    }

    private void assertChosen(List<UUID> expectedIds) throws Exception {
        ArgumentCaptor<InteractionAnswer> captor = ArgumentCaptor.forClass(InteractionAnswer.class);
        verify(gameActions).answerInteraction(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(InteractionAnswer.PermanentsChosen.class);
        assertThat(((InteractionAnswer.PermanentsChosen) captor.getValue()).permanentIds())
                .containsExactlyElementsOf(expectedIds);
    }

    private AiInteractionContext context() {
        return new AiInteractionContext(
                gameData, gameData.id, aiPlayerId, gameQueryService, gameActions);
    }

    private static PendingInteraction.MultiPermanentChoice multiChoice(
            UUID playerId, List<UUID> validIds, int maxCount) {
        return multiChoice(playerId, validIds, List.of(), maxCount);
    }

    private static PendingInteraction.MultiPermanentChoice multiChoice(
            UUID playerId, List<UUID> validIds, List<UUID> validPlayerIds, int maxCount) {
        return new PendingInteraction.MultiPermanentChoice(
                playerId, validIds, validPlayerIds, maxCount,
                new MultiPermanentChoiceContext.ExileDamagedPlayerControls(),
                "Choose permanents.");
    }

    private PendingInteraction.MultiPermanentChoice powerLimitedChoice(List<UUID> validIds, int maxPower) {
        return new PendingInteraction.MultiPermanentChoice(
                aiPlayerId, validIds, List.of(), validIds.size(),
                new MultiPermanentChoiceContext.EachPlayerChoosesCreaturesWithTotalPowerAtMostChoice(
                        aiPlayerId, maxPower, List.of(), List.of(), "Power-limited choice"),
                "Choose creatures to keep.");
    }

    private static Permanent creature(String name, int power) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(Math.max(1, power));
        return new Permanent(card);
    }
}
