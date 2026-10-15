package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValorSinger.class, DireWolfProwler.class})
class ValorSingerTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your combat, target creature you control gets +1/+0")
    void boostsTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new ValorSinger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        resolveBeginningOfCombat(player1, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger targets only creatures you control")
    void targetsOnlyCreaturesYouControl() {
        harness.addToBattlefield(player1, new ValorSinger());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        advanceToBeginningOfCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ValorSinger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        resolveBeginningOfCombat(player1, target);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost persists after combat ends")
    void boostPersistsAfterCombat() {
        harness.addToBattlefield(player1, new ValorSinger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        resolveBeginningOfCombat(player1, target);
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Valor Singer can target itself even while tapped")
    void canBoostItselfWhileTapped() {
        Permanent singer = harness.addToBattlefieldAndReturn(player1, new ValorSinger());
        singer.tap();

        resolveBeginningOfCombat(player1, singer);

        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, singer)).isEqualTo(3);
        assertThat(singer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Valor Singer does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent singer = harness.addToBattlefieldAndReturn(player1, new ValorSinger());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        advanceToBeginningOfCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, singer)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The second player's Singer boosts a creature on that player's turn")
    void triggersForSecondPlayerOnTheirTurn() {
        harness.addToBattlefield(player2, new ValorSinger());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());

        resolveBeginningOfCombat(player2, target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    private void resolveBeginningOfCombat(Player activePlayer, Permanent target) {
        advanceToBeginningOfCombat(activePlayer);
        harness.handlePermanentChosen(activePlayer, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
