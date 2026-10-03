package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.D20RollService;
import com.github.laxika.magicalvibes.service.effect.normalfx.RollD20EffectHandler;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DanseMacabre.class, GrizzlyBears.class, HillGiant.class, GrafdiggersCage.class})
class DanseMacabreTest extends BaseCardTest {

    private RollD20EffectHandler rollD20EffectHandler;
    private D20RollService originalD20RollService;

    @BeforeEach
    void captureD20RollService() {
        rollD20EffectHandler = GameTestEngineContext.get().getBean(RollD20EffectHandler.class);
        originalD20RollService = (D20RollService) ReflectionTestUtils.getField(
                rollD20EffectHandler, "d20RollService");
    }

    @AfterEach
    void restoreD20RollService() {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", originalD20RollService);
    }

    @Test
    void lowResultReturnsOneChosenSacrifice() {
        Permanent casterChoice = addCreatureReady(player1, new GrizzlyBears());
        Permanent casterAlternative = addCreatureReady(player1, new HillGiant());
        Permanent opposingChoice = addCreatureReady(player2, new GrizzlyBears());
        setRoll(12);

        castDanseMacabre();

        PendingInteraction.MultiPermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(sacrificeChoice.playerId()).isEqualTo(player1.getId());
        assertThat(sacrificeChoice.context()).isInstanceOf(MultiPermanentChoiceContext.DanseMacabreSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(casterChoice.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(casterAlternative.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(opposingChoice.getId()));

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.minCount()).isEqualTo(1);
        assertThat(returnChoice.maxCount()).isEqualTo(1);
        assertThat(returnChoice.validCardIds())
                .containsExactlyInAnyOrder(casterChoice.getCard().getId(), opposingChoice.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(opposingChoice.getCard().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opposingChoice.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(casterChoice.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opposingChoice.getCard().getId()));
    }

    @Test
    void highResultReturnsUpToTwoSacrificesUnderCasterControl() {
        Card casterCreature = new GrizzlyBears();
        Card opposingCreature = new HillGiant();
        addCreatureReady(player1, casterCreature);
        addCreatureReady(player2, opposingCreature);
        setRoll(13);

        castDanseMacabre();

        PendingInteraction.MultiGraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(returnChoice.minCount()).isZero();
        assertThat(returnChoice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(casterCreature.getId(), opposingCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(casterCreature.getId(), opposingCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(casterCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opposingCreature.getId()));
    }

    @Test
    void casterWithoutCreatureDoesNotAddOpponentsToughness() {
        Card opposingCreature = new HillGiant();
        addCreatureReady(player2, opposingCreature);
        setRoll(14);

        castDanseMacabre();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(opposingCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(opposingCreature.getId());
    }

    @Test
    void highResultAllowsReturningNoCreatures() {
        Card casterCreature = new GrizzlyBears();
        Card opposingCreature = new HillGiant();
        addCreatureReady(player1, casterCreature);
        addCreatureReady(player2, opposingCreature);
        setRoll(20);

        castDanseMacabre();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(casterCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void highResultAllowsReturningOnlyOneAndExcludesOlderGraveyardCards() {
        Card casterCreature = new GrizzlyBears();
        Card opposingCreature = new HillGiant();
        Card olderCreature = new HillGiant();
        addCreatureReady(player1, casterCreature);
        addCreatureReady(player2, opposingCreature);
        harness.setGraveyard(player1, List.of(olderCreature));
        setRoll(13);

        castDanseMacabre();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(casterCreature.getId(), opposingCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(casterCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(casterCreature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(olderCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    void addsModifiedToughnessBeforeSacrifice() {
        Permanent casterCreature = addCreatureReady(player1, new GrizzlyBears());
        casterCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card opposingCreature = new HillGiant();
        addCreatureReady(player2, opposingCreature);
        setRoll(12);

        castDanseMacabre();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1,
                List.of(casterCreature.getCard().getId(), opposingCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(casterCreature.getCard().getId(), opposingCreature.getId());
    }

    @Test
    void noCreaturesStillRollsAndCompletesWithoutReturnChoice() {
        setRoll(20);

        castDanseMacabre();

        assertThat(gameLogContains("rolls a d20 for Danse Macabre: 20")).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof DanseMacabre);
    }

    @Test
    void blockedOpposingCreatureReturnLeavesCardInOwnersGraveyard() {
        Card opposingCreature = new HillGiant();
        addCreatureReady(player2, opposingCreature);
        harness.addToBattlefield(player1, new GrafdiggersCage());
        setRoll(14);

        castDanseMacabre();
        harness.handleMultipleCardsChosen(player1, List.of(opposingCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(opposingCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(opposingCreature);
    }

    @Test
    void allPlayersChooseBeforeAnyCreatureIsSacrificed() {
        Permanent casterChoice = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());
        Permanent opposingChoice = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());
        setRoll(12);

        castDanseMacabre();
        harness.handleMultiplePermanentsChosen(player1, List.of(casterChoice.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(casterChoice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingChoice);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gameLogContains("rolls a d20 for Danse Macabre")).isFalse();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opposingChoice.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(casterChoice);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingChoice);
        harness.handleMultipleCardsChosen(player1, List.of(casterChoice.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingChoice.getCard());
    }

    private void castDanseMacabre() {
        harness.castFromHand(player1, new DanseMacabre(), "{3}{B}{B}");
        harness.passBothPriorities();
    }

    private void setRoll(int result) {
        ReflectionTestUtils.setField(rollD20EffectHandler, "d20RollService", new FixedD20RollService(result));
    }

    private static final class FixedD20RollService extends D20RollService {

        private final int result;

        private FixedD20RollService(int result) {
            this.result = result;
        }

        @Override
        public int roll() {
            return result;
        }
    }
}
