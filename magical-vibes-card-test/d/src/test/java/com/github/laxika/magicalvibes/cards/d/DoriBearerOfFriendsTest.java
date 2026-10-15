package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoriBearerOfFriends.class})
class DoriBearerOfFriendsTest extends BaseCardTest {

    @Test
    @DisplayName("When Dori enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new DoriBearerOfFriends()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Dori entering under the opponent's control creates Treasure for that opponent")
    void opponentReceivesTreasure() {
        harness.enterBattlefieldAndReturn(player2, new DoriBearerOfFriends());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, mode = EnumSource.Mode.EXCLUDE, names = "COLORLESS")
    @DisplayName("Dori's Treasure can immediately be sacrificed for one mana of any color")
    void treasureProducesChosenColor(ManaColor color) {
        harness.enterBattlefieldAndReturn(player1, new DoriBearerOfFriends());
        resolveAllTriggers();
        Permanent treasure = findPermanent(player1, "Treasure");
        var manaPool = gd.playerManaPools.get(player1.getId());
        int before = manaPool.get(color);
        int totalBefore = manaPool.getTotalAllMana();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);

        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertOnBattlefield(player1, "Dori, Bearer of Friends");
        assertThat(manaPool.get(color)).isEqualTo(before + 1);
        assertThat(manaPool.getTotalAllMana()).isEqualTo(totalBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dori can trample over a blocker while dying to its combat damage")
    void trampleDealsExcessDamage() {
        Permanent attacker = addCreatureReady(player1, new DoriBearerOfFriends());
        Permanent blocker = addCreatureReady(player2, new DoriBearerOfFriends());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 1));

        harness.assertLife(player2, 19);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player1, "Dori, Bearer of Friends");
        harness.assertInGraveyard(player2, "Dori, Bearer of Friends");
    }
}
