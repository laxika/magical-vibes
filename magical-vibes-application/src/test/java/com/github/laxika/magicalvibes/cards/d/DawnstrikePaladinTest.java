package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnstrikePaladin.class})
class DawnstrikePaladinTest extends BaseCardTest {

    @Test
    void unblockedAttackGainsLifeAndDoesNotTap() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent paladin = addCreatureReady(player1, new DawnstrikePaladin());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(paladin.isTapped()).isFalse();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void attackerAndBlockerGainLifeFromDamageToCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new DawnstrikePaladin());
        harness.addToBattlefield(player2, new DawnstrikePaladin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(attacker.isTapped()).isFalse();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 22);
        harness.assertOnBattlefield(player1, "Dawnstrike Paladin");
        harness.assertOnBattlefield(player2, "Dawnstrike Paladin");
    }
}
