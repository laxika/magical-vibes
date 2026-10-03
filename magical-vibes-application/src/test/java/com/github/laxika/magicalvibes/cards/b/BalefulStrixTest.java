package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalefulStrix.class, GrizzlyBears.class, Forest.class})
class BalefulStrixTest extends BaseCardTest {

    @Test
    void entersAndDrawsACard() {
        Card drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new BalefulStrix()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new BalefulStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void enteringWithoutBeingCastDrawsOnlyTheTopCardForItsController() {
        Card topCard = new BalefulStrix();
        Card secondCard = new BalefulStrix();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(topCard, secondCard));

        harness.enterBattlefieldAndReturn(player2, new BalefulStrix());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Baleful Strix");
    }

    @Test
    void deathtouchKillsALargerAttackerWhenBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Baleful Strix");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Baleful Strix");
        harness.assertLife(player2, 20);
    }

    @Test
    void flyingPreventsGroundBlockingButAllowsFlyingBlocking() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new BalefulStrix());
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        var defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(harness.getBlockLegalityService()
                .canBlockAttacker(gd, groundBlocker, attacker, defenders)).isFalse();
        assertThat(harness.getBlockLegalityService()
                .canBlockAttacker(gd, flyingBlocker, attacker, defenders)).isTrue();
    }
}
