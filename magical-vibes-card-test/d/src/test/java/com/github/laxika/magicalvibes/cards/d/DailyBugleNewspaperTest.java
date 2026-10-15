package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DailyBugleNewspaper.class})
class DailyBugleNewspaperTest extends BaseCardTest {

    @Test
    void drawsDiscardsAndCreatesTreasure() {
        Permanent newspaper = harness.addToBattlefieldAndReturn(player1, new DailyBugleNewspaper());
        Card discarded = new DailyBugleNewspaper();
        Card drawn = new DailyBugleNewspaper();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(newspaper.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Treasure");
    }

    @Test
    void canDiscardTheCardJustDrawn() {
        harness.addToBattlefield(player1, new DailyBugleNewspaper());
        Card kept = new DailyBugleNewspaper();
        Card drawn = new DailyBugleNewspaper();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void emptyHandStillDrawsDiscardsAndCreatesUsableTreasure() {
        harness.addToBattlefield(player1, new DailyBugleNewspaper());
        Card drawn = new DailyBugleNewspaper();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
