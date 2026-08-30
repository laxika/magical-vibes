package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueSliver.class, MetallicSliver.class, GrizzlyBears.class})
class PlagueSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Each Sliver deals 1 damage to its controller during that player's upkeep")
    void sliversDamageTheirControllers() {
        addCreatureReady(player1, new PlagueSliver());
        addCreatureReady(player1, new MetallicSliver());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new MetallicSliver());

        resolveUpkeep(player1);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The granted upkeep ability affects Slivers controlled by an opponent")
    void opponentsSliversDamageTheirController() {
        addCreatureReady(player1, new PlagueSliver());
        addCreatureReady(player2, new MetallicSliver());

        resolveUpkeep(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Slivers stop having the upkeep ability when Plague Sliver leaves")
    void grantedAbilityEndsWhenSourceLeaves() {
        var plagueSliver = addCreatureReady(player1, new PlagueSliver());
        addCreatureReady(player1, new MetallicSliver());
        gd.playerBattlefields.get(player1.getId()).remove(plagueSliver);

        resolveUpkeep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void resolveUpkeep(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();
    }
}
