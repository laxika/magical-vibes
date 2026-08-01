package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmyAntsTest extends BaseCardTest {

    @Test
    @DisplayName("With multiple lands, controller chooses which to sacrifice then destroys target")
    void choosesLandThenDestroysTarget() {
        Permanent ants = addReadyAnts(player1);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, 0, null, enemyLand.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, ownLand.getId());
        harness.passBothPriorities();

        assertThat(ants.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownLand);
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("With one land, auto-sacrifices it and destroys the targeted land")
    void autoSacrificesOnlyLand() {
        Permanent ants = addReadyAnts(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.activateAbility(player1, 0, null, enemyLand.getId());
        harness.passBothPriorities();

        assertThat(ants.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void requiresLandToSacrifice() {
        addReadyAnts(player1);
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonland() {
        addReadyAnts(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAnts(Player player) {
        Permanent perm = new Permanent(new ArmyAnts());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
