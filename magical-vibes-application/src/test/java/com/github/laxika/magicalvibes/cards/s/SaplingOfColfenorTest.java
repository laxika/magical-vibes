package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaplingOfColfenorTest extends BaseCardTest {

    private void attackWith(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attacker, List.of(0));
        harness.passBothPriorities(); // resolve the attack trigger
    }

    @Test
    @DisplayName("Revealing a creature gains toughness, loses power, and puts it into hand")
    void revealCreatureGainToughnessLosePowerToHand() {
        Permanent sapling = harness.addToBattlefieldAndReturn(player1, new SaplingOfColfenor());
        sapling.setSummoningSick(false);
        harness.setHand(player1, List.of());
        Card topCard = new GiantSpider(); // 2/4
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        attackWith(player1);

        // Gain 4 (toughness), lose 2 (power) => net +2.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("Revealing a non-creature leaves it on top and changes nothing")
    void revealNonCreatureDoesNothing() {
        Permanent sapling = harness.addToBattlefieldAndReturn(player1, new SaplingOfColfenor());
        sapling.setSummoningSick(false);
        harness.setHand(player1, List.of());
        Card topCard = new Shock(); // Instant
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        attackWith(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topCard.getId());
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void doesNothingWhenLibraryEmpty() {
        Permanent sapling = harness.addToBattlefieldAndReturn(player1, new SaplingOfColfenor());
        sapling.setSummoningSick(false);
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        harness.setLife(player1, 20);

        attackWith(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
