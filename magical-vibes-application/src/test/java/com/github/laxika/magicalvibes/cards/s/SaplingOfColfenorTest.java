package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.Primalcrux;
import com.github.laxika.magicalvibes.cards.u.Unmake;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaplingOfColfenor.class, GiantSpider.class, Shock.class, Primalcrux.class, Unmake.class})
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
        harness.setLibrary(player1, List.of(topCard));
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
        harness.setLibrary(player1, List.of(topCard));
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
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        attackWith(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Characteristic-defining power and toughness apply while the revealed creature is in the library")
    void revealedPrimalcruxGeneratesSeparateLifeEvents() {
        Permanent sapling = harness.addToBattlefieldAndReturn(player1, new SaplingOfColfenor());
        sapling.setSummoningSick(false);
        Card topCard = new Primalcrux();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        gd.lifeGainedThisTurn.clear();

        attackWith(player1);

        // Sapling's two hybrid symbols each count as a green mana symbol.
        harness.assertLife(player1, 20);
        assertThat(gd.lifeGainedThisTurn.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("loses 2 life"));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attacking controller reveals from their own library and receives the life changes")
    void opposingControllerUsesTheirOwnLibrary() {
        Permanent sapling = harness.addToBattlefieldAndReturn(player2, new SaplingOfColfenor());
        sapling.setSummoningSick(false);
        Card topCard = new SaplingOfColfenor();
        Card otherTopCard = new Unmake();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(otherTopCard));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        attackWith(player2);

        harness.assertLife(player2, 23);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherTopCard);
    }

    @Test
    @DisplayName("The attack trigger still resolves after Sapling is exiled")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent sapling = harness.addToBattlefieldAndReturn(player1, new SaplingOfColfenor());
        sapling.setSummoningSick(false);
        Card topCard = new SaplingOfColfenor();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Unmake()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        harness.castAndResolveInstant(player2, 0, sapling.getId());
        harness.assertNotOnBattlefield(player1, "Sapling of Colfenor");
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
