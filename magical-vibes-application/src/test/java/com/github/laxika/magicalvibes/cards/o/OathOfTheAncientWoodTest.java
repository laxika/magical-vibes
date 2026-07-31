package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OathOfTheAncientWoodTest extends BaseCardTest {

    private void castOath(Permanent target) {
        harness.setHand(player1, List.of(new OathOfTheAncientWood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target == null ? null : target.getId());
    }

    @Test
    @DisplayName("Its own entry puts a +1/+1 counter on the chosen creature when accepted")
    void selfEntryPutsCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOath(bears);

        harness.passBothPriorities(); // resolve the enchantment (queues the ETB may ability)
        harness.passBothPriorities(); // may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, bears.getId());
        }
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may leaves the creature without a counter")
    void decliningLeavesNoCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOath(bears);

        harness.passBothPriorities(); // resolve the enchantment (queues the ETB may ability)
        harness.passBothPriorities(); // may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Another enchantment entering queues a target choice, then puts the counter")
    void allyEnchantmentEntryPutsCounter() {
        harness.addToBattlefield(player1, new OathOfTheAncientWood());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // Glorious Anthem enters → Oath triggers

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-enchantment permanent entering does not trigger the ability")
    void creatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new OathOfTheAncientWood());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent on cast")
    void cannotTargetNonCreature() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new OathOfTheAncientWood()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, anthem.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
