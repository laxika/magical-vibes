package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LethargyTrap.class, GrizzlyBears.class})
class LethargyTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures get -3/-0 until end of turn")
    void weakensAttackingCreatures() {
        Permanent firstAttacker = addAttacker(new GrizzlyBears());
        Permanent secondAttacker = addAttacker(new GrizzlyBears());
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LethargyTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, nonAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("May cast for {U} when three creatures are attacking")
    void castsForAlternateCostWithThreeAttackers() {
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());
        Permanent thirdAttacker = addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new LethargyTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thirdAttacker)).isEqualTo(-1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost with fewer than three attackers")
    void alternateCostRequiresThreeAttackers() {
        addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new LethargyTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new LethargyTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Alternate cost remains paid when fewer creatures are attacking at resolution")
    void alternateCostDoesNotRequireThreeAttackersAtResolution() {
        Permanent firstAttacker = addAttacker(new GrizzlyBears());
        Permanent secondAttacker = addAttacker(new GrizzlyBears());
        Permanent removedFromCombat = addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new LethargyTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        removedFromCombat.setAttacking(false);
        removedFromCombat.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, removedFromCombat)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An affected creature keeps the reduction after leaving combat")
    void reductionPersistsAfterCreatureStopsAttacking() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new LethargyTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering attacking after resolution are not affected")
    void laterAttackerIsNotAffected() {
        Permanent attacker = addAttacker(new GrizzlyBears());
        harness.setHand(player1, List.of(new LethargyTrap()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);
        Permanent laterAttacker = addAttacker(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, laterAttacker)).isEqualTo(2);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
