package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManaWebTest extends BaseCardTest {

    @Test
    @DisplayName("A land tap queues Mana Web and resolution taps matching lands only")
    void tapsMatchingOpponentLandsOnResolution() {
        harness.addToBattlefield(player1, new ManaWeb());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        Permanent firstForest = gd.playerBattlefields.get(player2.getId()).get(0);
        Permanent secondForest = gd.playerBattlefields.get(player2.getId()).get(1);
        Permanent mountain = gd.playerBattlefields.get(player2.getId()).get(2);

        harness.tapPermanent(player2, 0);

        assertThat(firstForest.isTapped()).isTrue();
        assertThat(secondForest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isFalse();
        assertThat(gd.pendingManaAbilityTriggers).hasSize(1);

        resolveDeferredTriggers();

        assertThat(secondForest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana Web does not trigger when its controller taps a land")
    void doesNotTriggerForOwnLand() {
        harness.addToBattlefield(player1, new ManaWeb());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerBattlefields.get(player1.getId()).get(2).isTapped()).isFalse();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("Mana Web checks the triggering land's available types, not only the mana chosen")
    void checksAllTypesTheTriggeringLandCouldProduce() {
        harness.addToBattlefield(player1, new ManaWeb());
        harness.addToBattlefield(player2, new AdarkarWastes());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());

        Permanent wastes = gd.playerBattlefields.get(player2.getId()).get(0);
        Permanent forest = gd.playerBattlefields.get(player2.getId()).get(1);
        Permanent island = gd.playerBattlefields.get(player2.getId()).get(2);

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(wastes.isTapped()).isTrue();
        assertThat(forest.isTapped()).isFalse();
        assertThat(island.isTapped()).isFalse();

        resolveDeferredTriggers();

        assertThat(forest.isTapped()).isFalse();
        assertThat(island.isTapped()).isTrue();
    }

    private void resolveDeferredTriggers() {
        for (int i = 0; i < 4 && (!gd.stack.isEmpty() || !gd.pendingManaAbilityTriggers.isEmpty()); i++) {
            harness.passBothPriorities();
        }
    }
}
