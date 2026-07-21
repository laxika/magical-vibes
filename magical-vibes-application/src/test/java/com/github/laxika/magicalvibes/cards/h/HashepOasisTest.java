package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GraspingDunes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HashepOasisTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyOasis(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {G} produces green and costs 1 life")
    void tapPayLifeForGreen() {
        addReadyOasis(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pump ability sacrifices a Desert and gives target +3/+3 until end of turn")
    void pumpSacrificesDesertAndBoosts() {
        Permanent oasis = addReadyOasis(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Sole Desert — auto-sacrificed as cost.
        harness.activateAbility(player1, 0, 2, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(elemental.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(oasis.getId()));
        harness.assertInGraveyard(player1, "Hashep Oasis");
    }

    @Test
    @DisplayName("With multiple Deserts, controller chooses which to sacrifice")
    void choosesWhichDesertToSacrifice() {
        Permanent oasis = addReadyOasis(player1);
        Permanent otherDesert = new Permanent(new GraspingDunes());
        otherDesert.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherDesert);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 2, null, elemental.getId());
        harness.handlePermanentChosen(player1, otherDesert.getId());
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(oasis.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(otherDesert.getId()));
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpExpiresAtEndOfTurn() {
        addReadyOasis(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 2, null, elemental.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getPowerModifier()).isZero();
        assertThat(elemental.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadyOasis(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Pump ability can only be activated at sorcery speed")
    void pumpIsSorcerySpeedOnly() {
        addReadyOasis(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyOasis(Player player) {
        Permanent perm = new Permanent(new HashepOasis());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
