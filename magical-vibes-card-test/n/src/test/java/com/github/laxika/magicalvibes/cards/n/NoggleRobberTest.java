package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoggleRobber.class, WrathOfGod.class})
class NoggleRobberTest extends BaseCardTest {

    @Test
    @DisplayName("When Noggle Robber enters, it creates a Treasure token")
    void etbCreatesTreasureToken() {
        harness.setHand(player1, List.of(new NoggleRobber()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("When Noggle Robber dies, it creates a Treasure token")
    void deathCreatesTreasureToken() {
        harness.addToBattlefield(player1, new NoggleRobber());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
    }

    @Test
    @DisplayName("Simultaneous deaths create one Treasure for each Robber's controller")
    void simultaneousDeathsRewardEachController() {
        harness.addToBattlefield(player1, new NoggleRobber());
        harness.addToBattlefield(player1, new NoggleRobber());
        harness.addToBattlefield(player2, new NoggleRobber());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Noggle Robber")).isEmpty();
        assertThat(findPermanents(player2, "Noggle Robber")).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The Treasure can immediately be sacrificed for one mana of any color")
    void treasureProducesChosenColor(ManaColor color) {
        harness.setHand(player1, List.of(new NoggleRobber()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int manaBefore = gd.playerManaPools.get(player1.getId()).get(color);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(treasure), null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(manaBefore + 1);
        assertThat(gd.stack).isEmpty();
    }
}
