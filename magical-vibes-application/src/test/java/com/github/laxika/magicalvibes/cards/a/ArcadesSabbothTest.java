package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArcadesSabbothTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped creatures you control that are not attacking get +0/+2")
    void boostsUntappedNonattackingCreaturesYouControl() {
        Permanent arcades = addCreatureReady(player1, new ArcadesSabboth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, arcades)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The static bonus is removed while a creature is tapped or attacking")
    void staticBonusFollowsTapAndAttackState() {
        addCreatureReady(player1, new ArcadesSabboth());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bears.tap();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        bears.untap();
        bears.setAttacking(true);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The static bonus does not affect an opponent's creature")
    void doesNotBoostOpponentCreature() {
        addCreatureReady(player1, new ArcadesSabboth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {G}{W}{U} during upkeep keeps Arcades Sabboth on the battlefield")
    void payingUpkeepCostKeepsArcadesSabboth() {
        addCreatureReady(player1, new ArcadesSabboth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Arcades Sabboth")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Arcades Sabboth")
    void decliningUpkeepCostSacrificesArcadesSabboth() {
        addCreatureReady(player1, new ArcadesSabboth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Arcades Sabboth")).isZero();
    }

    @Test
    @DisplayName("{W} gives Arcades Sabboth +0/+1 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent arcades = addCreatureReady(player1, new ArcadesSabboth());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, arcades)).isEqualTo(10);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, arcades)).isEqualTo(9);
    }
}
