package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinLookout.class, GoblinTurncoat.class, AvenEnvoy.class})
class GoblinLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Goblin boosts every Goblin creature")
    void sacrificesGoblinAndBoostsAllGoblins() {
        Permanent lookout = addCreatureReady(player1, new GoblinLookout());
        Permanent sacrificedGoblin = addCreatureReady(player1, new GoblinTurncoat());
        Permanent ownGoblin = addCreatureReady(player1, new GoblinTurncoat());
        Permanent opponentGoblin = addCreatureReady(player2, new GoblinTurncoat());
        Permanent ownNonGoblin = addCreatureReady(player1, new AvenEnvoy());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificedGoblin);
        assertThat(lookout.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ownNonGoblin)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, lookout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownNonGoblin)).isEqualTo(2);
    }

    @Test
    @DisplayName("The sacrifice cost only accepts Goblins")
    void sacrificeCostOnlyAcceptsGoblins() {
        addCreatureReady(player1, new GoblinTurncoat());
        Permanent nonGoblin = addCreatureReady(player1, new AvenEnvoy());
        addCreatureReady(player1, new GoblinLookout());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @CardUsed(BoggartShenanigans.class)
    @DisplayName("Can sacrifice a noncreature permanent with the Goblin subtype")
    void canSacrificeNonCreatureGoblin() {
        Permanent lookout = addCreatureReady(player1, new GoblinLookout());
        Permanent kindredGoblin = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, kindredGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kindredGoblin);
        assertThat(lookout.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(3);
    }

    @Test
    @DisplayName("Goblin Lookout can sacrifice itself as the Goblin sacrifice")
    void canSacrificeItself() {
        Permanent lookout = addCreatureReady(player1, new GoblinLookout());
        Permanent remainingGoblin = addCreatureReady(player1, new GoblinTurncoat());
        Permanent opponentGoblin = addCreatureReady(player2, new GoblinTurncoat());
        Permanent nonGoblin = addCreatureReady(player1, new AvenEnvoy());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, lookout.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lookout);
        assertThat(gqs.getEffectivePower(gd, remainingGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate Goblin Lookout while it is tapped")
    void cannotActivateWhenTapped() {
        Permanent lookout = addCreatureReady(player1, new GoblinLookout());
        addCreatureReady(player1, new GoblinTurncoat());
        lookout.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The Goblin boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent lookout = addCreatureReady(player1, new GoblinLookout());
        Permanent sacrificedGoblin = addCreatureReady(player1, new GoblinTurncoat());
        Permanent opponentGoblin = addCreatureReady(player2, new GoblinTurncoat());
        Permanent nonGoblin = addCreatureReady(player1, new AvenEnvoy());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lookout)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGoblin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentGoblin)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonGoblin)).isEqualTo(0);
    }
}
