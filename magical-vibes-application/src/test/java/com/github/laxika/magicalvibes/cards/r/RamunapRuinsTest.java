package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GraspingDunes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RamunapRuinsTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyRuins(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {R} produces red and costs 1 life")
    void tapPayLifeForRed() {
        addReadyRuins(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage ability sacrifices a Desert and deals 2 to each opponent")
    void damageSacrificesDesert() {
        Permanent ruins = addReadyRuins(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Sole Desert — auto-sacrificed as cost.
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ruins.getId()));
        harness.assertInGraveyard(player1, "Ramunap Ruins");
    }

    @Test
    @DisplayName("With multiple Deserts, controller chooses which to sacrifice")
    void choosesWhichDesertToSacrifice() {
        Permanent ruins = addReadyRuins(player1);
        Permanent otherDesert = new Permanent(new GraspingDunes());
        otherDesert.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherDesert);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handlePermanentChosen(player1, otherDesert.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(ruins.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(otherDesert.getId()));
    }

    @Test
    @DisplayName("Damage ability can be activated at instant speed")
    void damageIsInstantSpeed() {
        addReadyRuins(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    private Permanent addReadyRuins(Player player) {
        Permanent perm = new Permanent(new RamunapRuins());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
