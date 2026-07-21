package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnathemancerTest extends BaseCardTest {

    private void castAnathemancerTargeting(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Anathemancer()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, targetPlayerId);

        harness.passBothPriorities(); // resolve creature spell — ETB triggers
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("ETB deals damage to target player equal to their nonbasic lands; basics don't count")
    void etbDamageEqualsNonbasicLandCount() {
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addToBattlefield(player2, new Forest()); // basic — ignored
        harness.setLife(player2, 20);

        castAnathemancerTargeting(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("ETB deals no damage when target player controls no nonbasic lands")
    void etbNoDamageWithOnlyBasics() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        castAnathemancerTargeting(player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB can target its own controller, counting that player's nonbasic lands")
    void etbCanTargetSelf() {
        harness.addToBattlefield(player1, new GhostQuarter());
        harness.setLife(player1, 20);

        castAnathemancerTargeting(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }
}
