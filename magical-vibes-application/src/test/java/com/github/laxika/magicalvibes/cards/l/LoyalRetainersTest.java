package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GuanYuSaintedWarrior;
import com.github.laxika.magicalvibes.cards.y.YoungWeiRecruits;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LoyalRetainers.class, GuanYuSaintedWarrior.class, YoungWeiRecruits.class})
class LoyalRetainersTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        addCreatureReady(player1, new LoyalRetainers());
        GuanYuSaintedWarrior guanYu = new GuanYuSaintedWarrior();
        harness.setGraveyard(player1, List.of(guanYu));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, guanYu.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Loyal Retainers");
        harness.assertInGraveyard(player1, "Loyal Retainers");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Returns a legendary creature card from the graveyard to the battlefield")
    void returnsLegendaryCreatureToBattlefield() {
        addCreatureReady(player1, new LoyalRetainers());
        GuanYuSaintedWarrior guanYu = new GuanYuSaintedWarrior();
        harness.setGraveyard(player1, List.of(guanYu));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, guanYu.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Guan Yu, Sainted Warrior");
        harness.assertNotInGraveyard(player1, "Guan Yu, Sainted Warrior");
    }

    @Test
    @DisplayName("A non-legendary creature in the graveyard is not a valid target")
    void cannotTargetNonLegendaryCreature() {
        addCreatureReady(player1, new LoyalRetainers());
        YoungWeiRecruits nonLegendary = new YoungWeiRecruits();
        GuanYuSaintedWarrior guanYu = new GuanYuSaintedWarrior();
        harness.setGraveyard(player1, List.of(nonLegendary, guanYu));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, nonLegendary.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target card");
    }

    @Test
    @DisplayName("Requires a legal graveyard target when activated")
    void requiresTargetWhenActivated() {
        addCreatureReady(player1, new LoyalRetainers());
        GuanYuSaintedWarrior guanYu = new GuanYuSaintedWarrior();
        harness.setGraveyard(player1, List.of(guanYu));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");

        harness.assertOnBattlefield(player1, "Loyal Retainers");
    }

    @Test
    @DisplayName("Cannot target a legendary creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        addCreatureReady(player1, new LoyalRetainers());
        GuanYuSaintedWarrior guanYu = new GuanYuSaintedWarrior();
        harness.setGraveyard(player2, List.of(guanYu));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, guanYu.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allowed graveyard");
        harness.assertOnBattlefield(player1, "Loyal Retainers");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addCreatureReady(player1, new LoyalRetainers());
        GuanYuSaintedWarrior guanYu = new GuanYuSaintedWarrior();
        harness.setGraveyard(player1, List.of(guanYu));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, guanYu.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    @DisplayName("Cannot activate after attackers are declared")
    void cannotActivateAfterAttackersDeclared() {
        addCreatureReady(player1, new LoyalRetainers());
        GuanYuSaintedWarrior guanYu = new GuanYuSaintedWarrior();
        harness.setGraveyard(player1, List.of(guanYu));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, guanYu.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before attackers are declared");
    }

}
