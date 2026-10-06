package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JadeMage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoodedHorror.class, JadeMage.class, Forest.class})
class HoodedHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked when the defending player controls the most creatures")
    void cannotBeBlockedWhenDefenderControlsMostCreatures() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new JadeMage());
        addCreatureReady(player2, new JadeMage());

        assertThat(bls.canBlockAttacker(gd, firstBlocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Cannot be blocked when the defending player is tied for most creatures")
    void cannotBeBlockedWhenDefenderIsTiedForMostCreatures() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new JadeMage());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Can be blocked when the defending player controls fewer creatures")
    void canBeBlockedWhenDefenderControlsFewerCreatures() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        addCreatureReady(player1, new JadeMage());
        Permanent blocker = addCreatureReady(player2, new JadeMage());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Creature counts are reevaluated when the battlefield changes")
    void blockabilityChangesWithCreatureCounts() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new JadeMage());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        addCreatureReady(player1, new JadeMage());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        addCreatureReady(player2, new JadeMage());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures count toward the defending player's total")
    void countsCreaturesThatCannotBlock() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        addCreatureReady(player1, new JadeMage());
        Permanent blocker = addCreatureReady(player2, new JadeMage());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new JadeMage());
        tappedCreature.tap();
        tappedCreature.setSummoningSick(true);

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Noncreature permanents do not increase the defending player's creature count")
    void doesNotCountNoncreaturePermanents() {
        Permanent horror = addCreatureReady(player1, new HoodedHorror());
        horror.setAttacking(true);
        addCreatureReady(player1, new JadeMage());
        Permanent blocker = addCreatureReady(player2, new JadeMage());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(bls.canBlockAttacker(gd, blocker, horror,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
