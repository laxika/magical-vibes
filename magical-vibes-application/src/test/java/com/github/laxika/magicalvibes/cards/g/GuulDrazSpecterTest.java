package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuulDrazSpecter.class, Forest.class})
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
        harness.setHand(player2, List.of(new Forest()));
        addAttackingSpecter();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("An empty controller hand does not grant the boost")
    void emptyControllerHandDoesNotGrantBoost() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        Permanent specter = harness.addToBattlefieldAndReturn(player1, new GuulDrazSpecter());

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost uses the Specter's controller to determine its opponent")
    void boostUsesCurrentController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        Permanent specter = harness.addToBattlefieldAndReturn(player2, new GuulDrazSpecter());

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(5);
    }

    @Test
    @DisplayName("Discarding the last card grants the boost after combat damage")
    void lastCardDiscardGrantsBoostAfterDamage() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player2, 20);
        Permanent specter = addAttackingSpecter();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(2);
        harness.handleCardChosen(player2, 0);

        assertThat(gqs.getEffectivePower(gd, specter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, specter)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The damaged player chooses exactly one card to discard")
    void damagedPlayerChoosesOneCard() {
        harness.setHand(player2, List.of(new Forest(), new GuulDrazSpecter()));
        addAttackingSpecter();

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player2, "Guul Draz Specter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Combat damage to an empty-handed player resolves without a discard prompt")
    void emptyHandDoesNotRequireDiscardChoice() {
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        addAttackingSpecter();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAttackingSpecter() {
        Permanent specter = addCreatureReady(player1, new GuulDrazSpecter());
        specter.setAttacking(true);
        return specter;
    }
}
