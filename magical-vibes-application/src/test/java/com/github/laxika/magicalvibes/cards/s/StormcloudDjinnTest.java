package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StormcloudDjinn.class, AirElemental.class, GrizzlyBears.class})
class StormcloudDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Stormcloud Djinn can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent djinn = addReadyDjinn(player2);
        addAttackingCreature(player1, new AirElemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(djinn.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Stormcloud Djinn cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addReadyDjinn(player2);
        addAttackingCreature(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Its ability gives it +2/+0 and deals 1 damage to its controller")
    void abilityBoostsAndDealsDamage() {
        Permanent djinn = addReadyDjinn(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(djinn.getPowerModifier()).isEqualTo(2);
        assertThat(djinn.getToughnessModifier()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The ability's power boost wears off at end of turn")
    void abilityBoostWearsOffAtEndOfTurn() {
        Permanent djinn = addReadyDjinn(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(djinn.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(djinn.getPowerModifier()).isZero();
    }

    private Permanent addReadyDjinn(Player player) {
        Permanent djinn = new Permanent(new StormcloudDjinn());
        djinn.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(djinn);
        return djinn;
    }

    private void addAttackingCreature(Player player, Card creature) {
        Permanent attacker = new Permanent(creature);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
    }
}
