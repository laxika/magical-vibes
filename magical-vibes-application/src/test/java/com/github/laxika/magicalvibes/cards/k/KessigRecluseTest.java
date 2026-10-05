package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.h.HollowhengeBeast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({KessigRecluse.class, AirElemental.class, HollowhengeBeast.class})
class KessigRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a flying creature due to reach")
    void canBlockFlyingCreature() {
        Permanent recluse = addCreatureReady(player2, new KessigRecluse());
        Permanent flyer = addAttackingFlyer();

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(recluse);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deathtouch kills a larger creature it damages in combat")
    void deathtouchKillsLargerCreature() {
        Permanent recluse = addCreatureReady(player2, new KessigRecluse());
        Permanent flyer = addAttackingFlyer();

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(recluse);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(flyer);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(flyer.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(recluse.getId()));
    }

    private Permanent addAttackingFlyer() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        flyer.setAttacking(true);
        return flyer;
    }

    @Test
    @DisplayName("Deathtouch also destroys a larger creature blocking the recluse")
    void deathtouchKillsLargerBlocker() {
        Permanent recluse = addCreatureReady(player1, new KessigRecluse());
        Permanent beast = addCreatureReady(player2, new HollowhengeBeast());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(beast.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(recluse.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(beast.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(recluse.getCard());
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An unblocked recluse deals ordinary damage to a player")
    void deathtouchDoesNotKillPlayer() {
        Permanent recluse = addCreatureReady(player1, new KessigRecluse());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(recluse);
    }
}
