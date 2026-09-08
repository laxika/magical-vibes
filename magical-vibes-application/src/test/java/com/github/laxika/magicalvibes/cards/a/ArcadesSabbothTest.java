package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArcadesSabbothTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped creatures that are not attacking get +0/+2, including Arcades Sabboth")
    void boostsUntappedNonAttackingCreatures() {
        Permanent arcades = harness.addToBattlefieldAndReturn(player1, new ArcadesSabboth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, arcades)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, arcades)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Tapped or attacking creatures do not get the static toughness bonus")
    void excludesTappedAndAttackingCreatures() {
        harness.addToBattlefield(player1, new ArcadesSabboth());
        Permanent tappedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent attackingBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedBears.tap();
        attackingBears.setAttacking(true);

        assertThat(gqs.getEffectiveToughness(gd, tappedBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attackingBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("White ability gives Arcades Sabboth +0/+1 until end of turn")
    void whiteAbilityBoostsSelfUntilEndOfTurn() {
        Permanent arcades = harness.addToBattlefieldAndReturn(player1, new ArcadesSabboth());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, arcades)).isEqualTo(10);
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Arcades Sabboth")
    void decliningUpkeepPaymentSacrificesArcades() {
        harness.addToBattlefield(player1, new ArcadesSabboth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Arcades Sabboth");
        harness.assertInGraveyard(player1, "Arcades Sabboth");
    }

    @Test
    @DisplayName("Paying the upkeep cost keeps Arcades Sabboth")
    void payingUpkeepCostKeepsArcades() {
        harness.addToBattlefield(player1, new ArcadesSabboth());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Arcades Sabboth");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during an opponent's upkeep")
    void noUpkeepTriggerOnOpponentsTurn() {
        harness.addToBattlefield(player1, new ArcadesSabboth());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arcades Sabboth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
