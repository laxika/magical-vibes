package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MaggotCarrier;
import com.github.laxika.magicalvibes.cards.p.PhyrexianTyranny;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderscapeBattlemage.class, MaggotCarrier.class, PhyrexianTyranny.class})
class ThunderscapeBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Without either kicker, neither ability resolves")
    void noKicker() {
        harness.setHand(player1, List.of(new ThunderscapeBattlemage()));
        addMana(2, ManaColor.RED);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thunderscape Battlemage");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Black kicker makes a target player discard two cards")
    void blackKickerDiscardsTwo() {
        harness.setHand(player2, List.of(new MaggotCarrier(), new MaggotCarrier(), new MaggotCarrier()));
        harness.setHand(player1, List.of(new ThunderscapeBattlemage()));
        addMana(3, ManaColor.RED, ManaColor.BLACK);

        harness.castKickedCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Green kicker destroys a target enchantment")
    void greenKickerDestroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianTyranny());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MaggotCarrier());
        harness.setHand(player2, List.of(new MaggotCarrier(), new MaggotCarrier()));
        harness.setHand(player1, List.of(new ThunderscapeBattlemage()));
        addMana(2, ManaColor.RED, ManaColor.GREEN);

        castWithAdditionalCosts(List.of("{G}"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enchantment.getId())
                .doesNotContain(creature.getId(), player1.getId());
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Phyrexian Tyranny");
        harness.assertInGraveyard(player2, "Phyrexian Tyranny");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Both kicker abilities resolve independently")
    void bothKickers() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianTyranny());
        harness.setHand(player2, List.of(new MaggotCarrier(), new MaggotCarrier(), new MaggotCarrier()));
        harness.setHand(player1, List.of(new ThunderscapeBattlemage()));
        addMana(3, ManaColor.RED, ManaColor.BLACK, ManaColor.GREEN);

        castWithAdditionalCosts(List.of("{G}"), player2.getId(), true);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player2, "Phyrexian Tyranny");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    private void addMana(int colorless, ManaColor... colored) {
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        for (ManaColor color : colored) {
            harness.addMana(player1, color, 1);
        }
    }

    private void castWithAdditionalCosts(List<String> payments) {
        castWithAdditionalCosts(payments, null, false);
    }

    private void castWithAdditionalCosts(List<String> payments, java.util.UUID targetId, boolean kicked) {
        gs.playCard(gd, player1, 0, 0, targetId, null, List.of(), List.of(), false,
                null, null, null, null, null, kicked, null, null, null, null,
                payments, false);
    }
}
