package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnyaroBees.class, GrizzlyBears.class})
class UnyaroBeesTest extends BaseCardTest {

    @Test
    @DisplayName("The green ability gives Unyaro Bees +1/+1 until end of turn")
    void boostsUntilEndOfTurn() {
        Permanent bees = addReadyBees(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(1);
        assertThat(bees.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bees.getEffectivePower()).isEqualTo(0);
        assertThat(bees.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The sacrifice ability is paid as a cost and deals 2 damage to a player")
    void sacrificesAsCostAndDealsDamageToPlayer() {
        addReadyBees(player1);
        harness.setLife(player2, 20);
        addSacrificeAbilityMana();

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Unyaro Bees");
        harness.assertInGraveyard(player1, "Unyaro Bees");
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The sacrifice ability deals 2 damage to a creature")
    void dealsDamageToCreature() {
        addReadyBees(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        addSacrificeAbilityMana();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private Permanent addReadyBees(Player player) {
        Permanent bees = new Permanent(new UnyaroBees());
        bees.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bees);
        return bees;
    }

    private void addSacrificeAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
