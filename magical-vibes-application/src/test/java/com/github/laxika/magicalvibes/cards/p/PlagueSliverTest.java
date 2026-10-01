package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.f.FungusSliver;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlagueSliver.class, FungusSliver.class, AshcoatBear.class})
class PlagueSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Each Sliver deals 1 damage to its controller during that player's upkeep")
    void sliversDamageTheirControllers() {
        addCreatureReady(player1, new PlagueSliver());
        addCreatureReady(player1, new FungusSliver());
        addCreatureReady(player1, new AshcoatBear());
        addCreatureReady(player2, new FungusSliver());

        resolveUpkeep(player1);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The granted upkeep ability affects Slivers controlled by an opponent")
    void opponentsSliversDamageTheirController() {
        addCreatureReady(player1, new PlagueSliver());
        addCreatureReady(player2, new FungusSliver());

        resolveUpkeep(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Slivers stop having the upkeep ability when Plague Sliver leaves")
    void grantedAbilityEndsWhenSourceLeaves() {
        var plagueSliver = addCreatureReady(player1, new PlagueSliver());
        addCreatureReady(player1, new FungusSliver());
        gd.playerBattlefields.get(player1.getId()).remove(plagueSliver);

        resolveUpkeep(player1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An upkeep trigger still resolves if Plague Sliver leaves after it triggers")
    void triggeredAbilityResolvesAfterSourceLeaves() {
        var plagueSliver = addCreatureReady(player1, new PlagueSliver());
        addCreatureReady(player1, new FungusSliver());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player1.getId()).remove(plagueSliver);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    private void resolveUpkeep(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();
    }
}
