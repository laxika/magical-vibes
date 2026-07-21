package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

class ShefetDunesTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyDunes(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {W} produces white and costs 1 life")
    void tapPayLifeForWhite() {
        addReadyDunes(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Pump ability sacrifices a Desert and gives creatures you control +1/+1 until end of turn")
    void pumpSacrificesDesertAndBoostsOwnCreatures() {
        Permanent dunes = addReadyDunes(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Sole Desert — auto-sacrificed as cost.
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(dunes.getId()));
        harness.assertInGraveyard(player1, "Shefet Dunes");
    }

    @Test
    @DisplayName("With multiple Deserts, controller chooses which to sacrifice")
    void choosesWhichDesertToSacrifice() {
        Permanent dunes = addReadyDunes(player1);
        Permanent otherDesert = new Permanent(new GraspingDunes());
        otherDesert.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherDesert);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handlePermanentChosen(player1, otherDesert.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(dunes.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(otherDesert.getId()));
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpExpiresAtEndOfTurn() {
        addReadyDunes(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Pump ability can only be activated at sorcery speed")
    void pumpIsSorcerySpeedOnly() {
        addReadyDunes(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyDunes(Player player) {
        Permanent perm = new Permanent(new ShefetDunes());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
