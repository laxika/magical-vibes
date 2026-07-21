package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GraspingDunes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IfnirDeadlandsTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C} produces colorless mana")
    void tapForColorless() {
        addReadyDeadlands(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{T}, Pay 1 life: Add {B} produces black and costs 1 life")
    void tapPayLifeForBlack() {
        addReadyDeadlands(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability sacrifices a Desert and puts two -1/-1 counters on opponent creature")
    void countersSacrificesDesert() {
        Permanent deadlands = addReadyDeadlands(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Sole Desert — auto-sacrificed as cost.
        harness.activateAbility(player1, 0, 2, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(deadlands.getId()));
        harness.assertInGraveyard(player1, "Ifnir Deadlands");
    }

    @Test
    @DisplayName("With multiple Deserts, controller chooses which to sacrifice")
    void choosesWhichDesertToSacrifice() {
        Permanent deadlands = addReadyDeadlands(player1);
        Permanent otherDesert = new Permanent(new GraspingDunes());
        otherDesert.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherDesert);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 2, null, elemental.getId());
        harness.handlePermanentChosen(player1, otherDesert.getId());
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(deadlands.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(otherDesert.getId()));
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        addReadyDeadlands(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Ability can only be activated at sorcery speed")
    void countersIsSorcerySpeedOnly() {
        addReadyDeadlands(player1);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, elemental.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyDeadlands(Player player) {
        Permanent perm = new Permanent(new IfnirDeadlands());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
