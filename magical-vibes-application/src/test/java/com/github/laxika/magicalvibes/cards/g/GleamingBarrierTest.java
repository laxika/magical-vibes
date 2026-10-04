package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GleamingBarrier.class, WrathOfGod.class})
class GleamingBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("When Gleaming Barrier dies, a Treasure token is created")
    void deathTriggerCreatesTreasureToken() {
        harness.addToBattlefield(player1, new GleamingBarrier());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gleaming Barrier");

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(gd.stack).isEmpty();
        assertThat(treasures).hasSize(1);
    }

    @Test
    @DisplayName("Gleaming Barrier's death-trigger Treasure is an artifact with the Treasure subtype")
    void deathTriggerCreatesTreasureWithCorrectProperties() {
        harness.addToBattlefield(player1, new GleamingBarrier());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");

        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(treasure.getCard().isToken()).isTrue();
    }
    @Test
    @DisplayName("Each simultaneously dying Barrier creates a Treasure for its own controller")
    void simultaneousDeathsCreateTreasuresForEachController() {
        harness.addToBattlefield(player1, new GleamingBarrier());
        harness.addToBattlefield(player1, new GleamingBarrier());
        harness.addToBattlefield(player2, new GleamingBarrier());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(countPermanents(player2, "Treasure")).isEqualTo(1);
        assertThat(findPermanents(player1, "Gleaming Barrier")).isEmpty();
        assertThat(findPermanents(player2, "Gleaming Barrier")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly created Treasure can immediately be sacrificed for colored mana")
    void treasureCanImmediatelyProduceMana() {
        harness.addToBattlefield(player1, new GleamingBarrier());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
