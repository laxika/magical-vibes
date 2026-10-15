package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoungWeiRecruits.class, ForestBear.class})
class YoungWeiRecruitsTest extends BaseCardTest {

    @Test
    @DisplayName("Young Wei Recruits cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new YoungWeiRecruits());

        addCreatureReady(player1, new ForestBear());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Young Wei Recruits can attack and deal combat damage")
    void canAttackAndDealCombatDamage() {
        addCreatureReady(player1, new YoungWeiRecruits());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Young Wei Recruits does not prevent another creature from blocking")
    void doesNotPreventOtherCreaturesFromBlocking() {
        addCreatureReady(player1, new ForestBear());
        addCreatureReady(player2, new YoungWeiRecruits());
        addCreatureReady(player2, new ForestBear());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Forest Bear");
        harness.assertInGraveyard(player2, "Forest Bear");
        harness.assertOnBattlefield(player2, "Young Wei Recruits");
    }
}
