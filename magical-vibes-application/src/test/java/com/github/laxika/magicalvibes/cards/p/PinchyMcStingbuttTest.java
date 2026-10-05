package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DeathOfAThousandStings;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LethalSting;
import com.github.laxika.magicalvibes.cards.s.StingingShot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PinchyMcStingbutt.class, DeathOfAThousandStings.class, LethalSting.class,
        StingingShot.class, GrizzlyBears.class})
class PinchyMcStingbuttTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage conjures one sting card into hand and reveals it")
    void combatDamageConjuresAndRevealsRandomStingCard() {
        harness.setHand(player1, List.of());
        Permanent pinchy = addCreatureReady(player1, new PinchyMcStingbutt());
        pinchy.setAttacking(true);
        pinchy.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .extracting(Card::getName)
                .isIn("Death of a Thousand Stings", "Lethal Sting", "Stinging Shot");
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"REVEAL_HAND\""))
                .anyMatch(message -> message.contains("Death of a Thousand Stings")
                        || message.contains("Lethal Sting")
                        || message.contains("Stinging Shot"));
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"REVEAL_HAND\""))
                .anyMatch(message -> message.contains("Death of a Thousand Stings")
                        || message.contains("Lethal Sting")
                        || message.contains("Stinging Shot"));
    }

    @Test
    @DisplayName("Combat damage trigger does not fire when Pinchy is blocked")
    void blockedCombatDamageDoesNotConjure() {
        harness.setHand(player1, List.of());
        Permanent pinchy = addCreatureReady(player1, new PinchyMcStingbutt());
        pinchy.setAttacking(true);
        pinchy.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attacking controller receives and owns the conjured card")
    void opponentControlledPinchyConjuresIntoOpponentsHand() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        Permanent pinchy = addCreatureReady(player2, new PinchyMcStingbutt());
        pinchy.setAttacking(true);
        pinchy.setAttackTarget(player1.getId());

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).singleElement().satisfies(card -> {
            assertThat(card.getName())
                    .isIn("Death of a Thousand Stings", "Lethal Sting", "Stinging Shot");
            assertThat(card.getOwnerId()).isEqualTo(player2.getId());
        });
    }

    @Test
    @DisplayName("Each Pinchy dealing combat damage conjures a separate card")
    void multipleUnblockedPinchysEachConjure() {
        harness.setHand(player1, List.of());
        for (int i = 0; i < 2; i++) {
            Permanent pinchy = addCreatureReady(player1, new PinchyMcStingbutt());
            pinchy.setAttacking(true);
            pinchy.setAttackTarget(player2.getId());
        }

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).allSatisfy(card -> {
            assertThat(card.getName())
                    .isIn("Death of a Thousand Stings", "Lethal Sting", "Stinging Shot");
            assertThat(card.getOwnerId()).isEqualTo(player1.getId());
        });
        assertThat(gd.playerHands.get(player1.getId()).get(0).getId())
                .isNotEqualTo(gd.playerHands.get(player1.getId()).get(1).getId());
    }
}
