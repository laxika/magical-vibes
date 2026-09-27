package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KemuriOnna.class, HandOfHonor.class, KamiOfTheCrescentMoon.class,
        SpiritualVisit.class})
class KemuriOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, target player discards a card")
    void entersAndTargetPlayerDiscards() {
        harness.setHand(player2, List.of(new HandOfHonor()));
        harness.setHand(player1, List.of(new KemuriOnna()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Hand of Honor");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Kemuri-Onna to its owner's hand")
    void spiritSpellReturnsKemuriOnna() {
        addKemuriOnna();
        harness.castFromHand(player1, new KamiOfTheCrescentMoon(), "{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Kemuri-Onna to its owner's hand")
    void arcaneSpellReturnsKemuriOnna() {
        addKemuriOnna();
        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Kemuri-Onna on the battlefield")
    void decliningCastTriggerLeavesKemuriOnnaOnBattlefield() {
        addKemuriOnna();
        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Kemuri-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addKemuriOnna();
        harness.castFromHand(player1, new HandOfHonor(), "{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("An opponent's Spirit spell does not trigger Kemuri-Onna")
    void opponentsSpiritSpellDoesNotTrigger() {
        addKemuriOnna();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new KamiOfTheCrescentMoon(), "{U}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Kemuri-Onna");
    }

    @Test
    @DisplayName("The ETB ability cannot target a permanent")
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new HandOfHonor());
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
