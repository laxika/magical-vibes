package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Woebearer.class, AlphaMyr.class, Swamp.class})
class WoebearerTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage lets the controller return a creature card to hand")
    void dealingCombatDamageReturnsCreatureCardToHand() {
        Card target = new AlphaMyr();
        harness.setGraveyard(player1, List.of(target));
        harness.setLife(player2, 20);
        attackWithWoebearerDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The controller may decline the graveyard return")
    void decliningLeavesCreatureCardInGraveyard() {
        Card target = new AlphaMyr();
        harness.setGraveyard(player1, List.of(target));
        harness.setLife(player2, 20);
        attackWithWoebearerDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("The trigger only offers creature cards")
    void onlyCreatureCardsCanBeReturned() {
        Card noncreature = new Swamp();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setLife(player2, 20);
        attackWithWoebearerDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(noncreature.getId()));
    }

    @Test
    @DisplayName("The trigger cannot return a creature card from an opponent's graveyard")
    void cannotReturnCreatureFromOpponentsGraveyard() {
        Card opponentCreature = new AlphaMyr();
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setLife(player2, 20);
        attackWithWoebearerDealingDamage();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("The trigger does not fire when Woebearer deals combat damage only to a blocker")
    void doesNotTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent woebearer = addCreatureReady(player1, new Woebearer());
        woebearer.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void attackWithWoebearerDealingDamage() {
        Permanent woebearer = addCreatureReady(player1, new Woebearer());
        woebearer.setAttacking(true);
        resolveCombat();
    }
}
