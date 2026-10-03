package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedShocker.class, AshcoatBear.class, SoulsFire.class})
class BarbedShockerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard their hand and draw that many cards")
    void combatDamageReplacesDamagedPlayersHand() {
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear()));
        Permanent shocker = addCreatureReady(player1, new BarbedShocker());
        shocker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when Barbed Shocker deals no combat damage to a player")
    void blockedShockerDoesNotTrigger() {
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear()));
        Permanent shocker = addCreatureReady(player1, new BarbedShocker());
        shocker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Noncombat damage to a player also triggers the hand replacement")
    void noncombatDamageTriggersHandReplacement() {
        Permanent shocker = addCreatureReady(player1, new BarbedShocker());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new AshcoatBear(), new AshcoatBear()));
        harness.setLibrary(player2, List.of(new AshcoatBear(), new AshcoatBear()));

        harness.castAndResolveInstant(player1, 0, List.of(shocker.getId(), player2.getId()));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty hand does not cause any cards to be drawn")
    void emptyHandDrawsNothing() {
        harness.setHand(player2, List.of());
        AshcoatBear libraryCard = new AshcoatBear();
        harness.setLibrary(player2, List.of(libraryCard));
        Permanent shocker = addCreatureReady(player1, new BarbedShocker());
        shocker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("The trigger uses the hand at resolution and survives its source leaving")
    void triggerUsesCurrentHandAfterSourceLeaves() {
        harness.setHand(player2, List.of(new AshcoatBear()));
        Permanent shocker = addCreatureReady(player1, new BarbedShocker());
        shocker.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(shocker);
        AshcoatBear firstDiscard = new AshcoatBear();
        AshcoatBear secondDiscard = new AshcoatBear();
        AshcoatBear thirdDiscard = new AshcoatBear();
        harness.setHand(player2, List.of(firstDiscard, secondDiscard, thirdDiscard));
        AshcoatBear firstDraw = new AshcoatBear();
        AshcoatBear secondDraw = new AshcoatBear();
        AshcoatBear thirdDraw = new AshcoatBear();
        harness.setLibrary(player2, List.of(firstDraw, secondDraw, thirdDraw));

        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstDiscard, secondDiscard, thirdDiscard);
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage to its own controller replaces that player's hand")
    void damageToControllerReplacesControllersHand() {
        Permanent shocker = addCreatureReady(player1, new BarbedShocker());
        AshcoatBear discardedCard = new AshcoatBear();
        AshcoatBear drawnCard = new AshcoatBear();
        harness.setHand(player1, List.of(new SoulsFire(), discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new AshcoatBear()));

        harness.castAndResolveInstant(player1, 0, List.of(shocker.getId(), player1.getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
