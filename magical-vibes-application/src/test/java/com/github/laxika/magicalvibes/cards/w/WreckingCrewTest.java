package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WreckingCrew.class, CloudSprite.class, GrizzlyBears.class})
class WreckingCrewTest extends BaseCardTest {

    @Test
    @DisplayName("Wrecking Crew can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent attacker = addReady(player1, new CloudSprite());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new WreckingCrew());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cloud Sprite");
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Wrecking Crew assigns excess combat damage to the defending player")
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addReady(player1, new WreckingCrew());
        attacker.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
