package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbedShocker.class, Forest.class, GrizzlyBears.class})
class BarbedShockerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard their hand and draw that many cards")
    void combatDamageReplacesDamagedPlayersHand() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        Permanent shocker = harness.addToBattlefieldAndReturn(player1, new BarbedShocker());
        shocker.setSummoningSick(false);
        shocker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // The extra card is the damaged player's normal draw after combat advances to their turn.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not trigger when Barbed Shocker deals no combat damage to a player")
    void blockedShockerDoesNotTrigger() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        Permanent shocker = harness.addToBattlefieldAndReturn(player1, new BarbedShocker());
        shocker.setSummoningSick(false);
        shocker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
