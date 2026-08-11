package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuietContemplationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell lets you pay {1} to tap and lock an opponent creature")
    void noncreatureSpellTapsAndLocksOpponentCreature() {
        harness.addToBattlefield(player1, new QuietContemplation());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to pay leaves the opponent creature unchanged")
    void decliningPaymentLeavesCreatureUnchanged() {
        harness.addToBattlefield(player1, new QuietContemplation());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Quiet Contemplation")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new QuietContemplation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Quiet Contemplation"));
    }

    @Test
    @DisplayName("The trigger cannot target a creature its controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new QuietContemplation());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
