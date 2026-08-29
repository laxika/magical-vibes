package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AetherstormRocTest extends BaseCardTest {

    @Test
    @DisplayName("Gains an energy counter when it enters and when another creature enters")
    void gainsEnergyFromCreatureEntries() {
        harness.setHand(player1, List.of(new AetherstormRoc()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay energy on attack to grow and tap a defending creature")
    void paysEnergyOnAttack() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the attack cost without enough energy")
    void cannotPayWithoutEnoughEnergy() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(victim.isTapped()).isFalse();
    }
}
