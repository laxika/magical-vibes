package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedShocker.class, AshcoatBear.class, Forest.class})
class BarbedShockerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard their hand and draw that many cards")
    void combatDamageReplacesDamagedPlayersHand() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
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
        harness.setHand(player2, List.of(new Forest(), new Forest()));
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
    @CardUsed(SoulsFire.class)
    @DisplayName("Noncombat damage to a player also triggers the hand replacement")
    void noncombatDamageTriggersHandReplacement() {
        Permanent shocker = addCreatureReady(player1, new BarbedShocker());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castAndResolveInstant(player1, 0, List.of(shocker.getId(), player2.getId()));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
