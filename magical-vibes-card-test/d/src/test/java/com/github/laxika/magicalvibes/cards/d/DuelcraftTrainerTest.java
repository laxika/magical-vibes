package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuelcraftTrainer.class, UnrulyMob.class, VampireInterloper.class})
class DuelcraftTrainerTest extends BaseCardTest {

    @Test
    @DisplayName("Coven lets the trainer give a controlled creature double strike")
    void grantsDoubleStrikeWithCoven() {
        harness.addToBattlefield(player1, new DuelcraftTrainer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new UnrulyMob());

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Coven does not trigger without three different powers")
    void doesNotGrantDoubleStrikeWithoutCoven() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new DuelcraftTrainer());
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new VampireInterloper());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trainer, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Coven targets only a creature controlled by the trainer's controller")
    void targetsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new DuelcraftTrainer());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new UnrulyMob());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new VampireInterloper());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("Granted double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DuelcraftTrainer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new UnrulyMob());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The trainer can target itself with coven")
    void canGrantDoubleStrikeToItself() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new DuelcraftTrainer());
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new UnrulyMob());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, trainer.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trainer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Coven is checked again when the combat ability resolves")
    void doesNotGrantDoubleStrikeWhenCovenIsLostBeforeResolution() {
        harness.addToBattlefield(player1, new DuelcraftTrainer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VampireInterloper());
        Permanent thirdPower = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, thirdPower));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Coven does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new DuelcraftTrainer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new UnrulyMob());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, trainer, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Duplicate powers do not prevent coven when three distinct powers remain")
    void grantsDoubleStrikeWithAdditionalDuplicatePower() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new DuelcraftTrainer());
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new VampireInterloper());
        harness.addToBattlefield(player1, new UnrulyMob());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, trainer.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, trainer, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
