package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoodedHorror.class, GrizzlyBears.class})
class HoodedHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked when the defending player controls the most creatures")
    void cannotBeBlockedWhenDefenderControlsMostCreatures() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, firstBlocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Cannot be blocked when the defending player is tied for most creatures")
    void cannotBeBlockedWhenDefenderIsTiedForMostCreatures() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Can be blocked when the defending player controls fewer creatures")
    void canBeBlockedWhenDefenderControlsFewerCreatures() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
