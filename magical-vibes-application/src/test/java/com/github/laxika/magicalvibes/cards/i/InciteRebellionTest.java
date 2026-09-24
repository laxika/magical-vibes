package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantSpider.class, GrizzlyBears.class, HillGiant.class, InciteRebellion.class, Mountain.class})
class InciteRebellionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals each player and their creatures damage equal to that player's creature count")
    void dealsDamageBasedOnEachPlayersCreatureCount() {
        Permanent ownSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent ownMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent opponentMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new InciteRebellion()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(ownSpider.getMarkedDamage()).isEqualTo(2);
        assertThat(ownGiant.getMarkedDamage()).isEqualTo(2);
        assertThat(opponentSpider.getMarkedDamage()).isEqualTo(1);
        assertThat(ownMountain.getMarkedDamage()).isZero();
        assertThat(opponentMountain.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Kills creatures when their controller's count is lethal")
    void killsCreaturesWithLethalDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InciteRebellion()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Incite Rebellion", "Grizzly Bears", "Grizzly Bears");
    }
}
