package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CankerousThirstTest extends BaseCardTest {

    private Permanent castAt(Permanent target, ManaColor color, int amount) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CankerousThirst()));
        harness.addMana(player1, color, amount);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        return target;
    }

    @Test
    @DisplayName("{B} spent, accepted: target creature gets -3/-3")
    void blackSpentShrinks() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4
        castAt(creature, ManaColor.BLACK, 4); // {3}{B/G} all black → only {B} spent
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("{G} spent, accepted: target creature gets +3/+3")
    void greenSpentPumps() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental()); // 4/4
        castAt(creature, ManaColor.GREEN, 4); // {3}{B/G} all green → only {G} spent
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.getEffectivePower()).isEqualTo(7);
        assertThat(creature.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("{B} spent, declined: target creature unchanged")
    void blackSpentDeclined() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4
        castAt(creature, ManaColor.BLACK, 4);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("{B} and {G} both spent: both clauses apply, netting 0/0")
    void bothColorsSpent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental()); // 4/4

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CankerousThirst()));
        // {3} generic paid across colors and the {B/G} hybrid the other → both {B} and {G} spent.
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true); // -3/-3
        harness.handleMayAbilityChosen(player1, true); // +3/+3

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental()); // 4/4
        castAt(creature, ManaColor.GREEN, 4);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(creature.getEffectivePower()).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Target must be a creature")
    void targetMustBeCreature() {
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CankerousThirst()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Forest")))
                .isInstanceOf(IllegalStateException.class);
    }
}
