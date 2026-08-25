package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PardicArsonist.class, GrizzlyBears.class})
class PardicArsonistTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold grants an ETB ability that deals 3 damage to a creature")
    void thresholdDealsDamageToCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var target = harness.getPermanentId(player2, "Grizzly Bears");
        castPardicArsonist();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Threshold-granted ETB ability can deal 3 damage to a player")
    void thresholdDealsDamageToPlayer() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.setLife(player2, 20);
        castPardicArsonist();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Pardic Arsonist has no ETB ability below threshold")
    void thresholdDoesNotGrantAbilityBelowSevenCards() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player2, 20);
        castPardicArsonist();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Pardic Arsonist");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPardicArsonist() {
        harness.setHand(player1, List.of(new PardicArsonist()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears());
    }
}
