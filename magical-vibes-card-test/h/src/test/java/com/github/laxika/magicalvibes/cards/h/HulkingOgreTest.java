package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HulkingOgre.class, GrizzlyBears.class})
class HulkingOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Hulking Ogre cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new HulkingOgre());

        addCreatureReady(player1, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Hulking Ogre can attack and deal combat damage")
    void canAttackAndDealCombatDamage() {
        addCreatureReady(player1, new HulkingOgre());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }
}
