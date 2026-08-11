package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuulDrazSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +3/+3 while an opponent has no cards in hand")
    void getsBoostWithEmptyOpponentHand() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        Permanent specter = harness.addToBattlefieldAndReturn(player1, new GuulDrazSpecter());

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not get the boost while every opponent has a card in hand")
    void noBoostWithCardsInOpponentHand() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        Permanent specter = harness.addToBattlefieldAndReturn(player1, new GuulDrazSpecter());

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses the boost when an opponent draws a card")
    void boostTracksOpponentHandChanges() {
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        Permanent specter = harness.addToBattlefieldAndReturn(player1, new GuulDrazSpecter());

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(5);
        harness.setHand(player2, List.of(new Forest()));

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage makes the damaged player discard a card")
    void combatDamageMakesDamagedPlayerDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        Permanent specter = addAttackingSpecter();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    private Permanent addAttackingSpecter() {
        Permanent specter = addCreatureReady(player1, new GuulDrazSpecter());
        specter.setAttacking(true);
        return specter;
    }
}
