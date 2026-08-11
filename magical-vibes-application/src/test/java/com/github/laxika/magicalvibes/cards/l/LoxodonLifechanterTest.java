package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoxodonLifechanterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may set life to the total toughness of creatures you control")
    void etbMaySetLifeToControlledCreatureToughness() {
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.setHand(player1, List.of(new LoxodonLifechanter()));
        harness.setLife(player1, 5);
        addLoxodonMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 8);
    }

    @Test
    @DisplayName("ETB life-total change can be declined")
    void etbLifeTotalChangeCanBeDeclined() {
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.setHand(player1, List.of(new LoxodonLifechanter()));
        harness.setLife(player1, 5);
        addLoxodonMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 5);
    }

    @Test
    @DisplayName("Activated ability uses the current life total for X")
    void activatedAbilityUsesCurrentLifeTotal() {
        Permanent loxodon = harness.addToBattlefieldAndReturn(player1, new LoxodonLifechanter());
        loxodon.setSummoningSick(false);
        harness.setLife(player1, 7);
        addLoxodonMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(loxodon.getEffectivePower()).isEqualTo(11);
        assertThat(loxodon.getEffectiveToughness()).isEqualTo(13);
    }

    private void addLoxodonMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
