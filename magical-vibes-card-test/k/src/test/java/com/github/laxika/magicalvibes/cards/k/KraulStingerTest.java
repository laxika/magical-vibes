package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KraulStinger.class, ColossalDreadmaw.class})
class KraulStingerTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent stinger = addCreatureReady(player1, new KraulStinger());
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(stinger)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(stinger))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stinger);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger attacker while blocking")
    void deathtouchDestroysLargerAttacker() {
        Permanent attacker = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent stinger = addCreatureReady(player2, new KraulStinger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(stinger);
        harness.assertInGraveyard(player1, "Colossal Dreadmaw");
        harness.assertInGraveyard(player2, "Kraul Stinger");
    }

    @Test
    @DisplayName("Zero power deals no damage and does not destroy a blocker")
    void zeroPowerDoesNotDestroyBlocker() {
        Permanent stinger = addCreatureReady(player1, new KraulStinger());
        stinger.setPowerModifier(-2);
        Permanent blocker = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(stinger);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertNotInGraveyard(player2, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("Deathtouch deals ordinary combat damage to a player")
    void unblockedAttackDealsOrdinaryDamage() {
        addCreatureReady(player1, new KraulStinger());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Kraul Stinger");
    }
}
