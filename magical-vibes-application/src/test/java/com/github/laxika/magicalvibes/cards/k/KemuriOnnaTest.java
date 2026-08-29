package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KemuriOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, target player discards a card")
    void entersAndTargetPlayerDiscards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, List.of(new KemuriOnna()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Kemuri-Onna to its owner's hand")
    void spiritSpellReturnsKemuriOnna() {
        addKemuriOnna();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Kemuri-Onna to its owner's hand")
    void arcaneSpellReturnsKemuriOnna() {
        addKemuriOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Kemuri-Onna on the battlefield")
    void decliningCastTriggerLeavesKemuriOnnaOnBattlefield() {
        addKemuriOnna();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Kemuri-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addKemuriOnna();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        harness.assertOnBattlefield(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("The ETB ability cannot target a permanent")
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KemuriOnna()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addKemuriOnna() {
        harness.addToBattlefield(player1, new KemuriOnna());
    }
}
