package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoxiousGroodion.class, AlpineGrizzly.class, AxebaneBeast.class})
class NoxiousGroodionTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch kills a larger creature it damages in combat")
    void deathtouchKillsLargerCreature() {
        Permanent groodion = addCreatureReady(player2, new NoxiousGroodion());
        Permanent attacker = addCreatureReady(player1, new AlpineGrizzly());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(groodion);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(groodion.getId()));
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker with more toughness than the damage dealt")
    void deathtouchDestroysHighToughnessBlocker() {
        Permanent groodion = addCreatureReady(player1, new NoxiousGroodion());
        Permanent blocker = addCreatureReady(player2, new AxebaneBeast());
        groodion.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(groodion);
        harness.assertInGraveyard(player2, "Axebane Beast");
        harness.assertInGraveyard(player1, "Noxious Groodion");
    }

    @Test
    @DisplayName("Deathtouch does not destroy a creature when no damage is dealt")
    void zeroPowerDoesNotDestroyAttacker() {
        Permanent groodion = addCreatureReady(player2, new NoxiousGroodion());
        groodion.setPowerModifier(-2);
        Permanent attacker = addCreatureReady(player1, new AxebaneBeast());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(groodion);
        harness.assertInGraveyard(player2, "Noxious Groodion");
    }
}
