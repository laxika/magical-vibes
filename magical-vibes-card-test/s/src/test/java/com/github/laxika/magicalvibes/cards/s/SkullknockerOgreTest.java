package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GraviticPunch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullknockerOgre.class, Forest.class, GrizzlyBears.class, GraviticPunch.class})
class SkullknockerOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the opponent discard at random and draw a card")
    void combatDamageMakesOpponentDiscardThenDraw() {
        addAttackingOgre(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        Card drawnCard = new Forest();
        harness.setLibrary(player2, List.of(drawnCard));

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("An empty opponent hand produces no draw")
    void emptyOpponentHandDoesNothing() {
        addAttackingOgre(player1);
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player2, List.of(new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Blocked combat damage does not trigger the ability")
    void blockedCombatDoesNotTrigger() {
        Permanent ogre = addAttackingOgre(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(ogre.isAttacking()).isFalse();
    }

    @Test
    @CardUsed({SkullknockerOgre.class, GraviticPunch.class, Forest.class})
    @DisplayName("Noncombat damage to an opponent also causes discard and draw")
    void noncombatDamageMakesOpponentDiscardThenDraw() {
        Permanent ogre = addCreatureReady(player1, new SkullknockerOgre());
        Card discardedCard = new Forest();
        Card drawnCard = new Forest();
        harness.setHand(player2, List.of(discardedCard));
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player1, List.of(new GraviticPunch()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(ogre.getId(), player2.getId()));
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    @CardUsed({SkullknockerOgre.class, GraviticPunch.class, Forest.class})
    @DisplayName("Damage to the Ogre's controller does not cause discard or draw")
    void damageToControllerDoesNotTrigger() {
        Permanent ogre = addCreatureReady(player1, new SkullknockerOgre());
        Card retainedCard = new Forest();
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of(new GraviticPunch(), retainedCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(ogre.getId(), player1.getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retainedCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("One damage event discards exactly one card from a larger hand")
    void discardsExactlyOneCardFromLargerHand() {
        addAttackingOgre(player1);
        Card firstCard = new Forest();
        Card secondCard = new Forest();
        Card drawnCard = new Forest();
        harness.setHand(player2, List.of(firstCard, secondCard));
        harness.setLibrary(player2, List.of(drawnCard));

        resolveCombatAndTrigger();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1)
                .allMatch(card -> card == firstCard || card == secondCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2).contains(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingOgre(Player player) {
        Permanent ogre = addCreatureReady(player, new SkullknockerOgre());
        ogre.setAttacking(true);
        return ogre;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
