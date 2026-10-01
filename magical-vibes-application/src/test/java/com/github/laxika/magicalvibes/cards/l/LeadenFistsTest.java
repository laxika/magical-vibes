package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeadenFists.class, FomoriNomad.class, HorizonCanopy.class})
class LeadenFistsTest extends BaseCardTest {

    @Test
    void canBeCastAtInstantSpeed() {
        Permanent creature = addCreatureReady(player1, new FomoriNomad());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LeadenFists()));
        addLeadenFistsMana(player1);
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(creature.getId());
    }

    @Test
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new FomoriNomad());

        harness.setHand(player1, List.of(new LeadenFists()));
        addLeadenFistsMana(player1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    void enchantedCreatureDoesNotUntapDuringItsControllersUntapStep() {
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        creature.tap();

        harness.setHand(player1, List.of(new LeadenFists()));
        addLeadenFistsMana(player1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void enchantedOpponentsCreatureDoesNotUntapDuringItsControllersUntapStep() {
        Permanent creature = addCreatureReady(player2, new FomoriNomad());
        creature.tap();

        harness.setHand(player1, List.of(new LeadenFists()));
        addLeadenFistsMana(player1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HorizonCanopy());
        harness.setHand(player1, List.of(new LeadenFists()));
        addLeadenFistsMana(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addLeadenFistsMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

}
