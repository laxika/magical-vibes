package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EnslavedDwarf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PardicArsonist.class, EnslavedDwarf.class})
class PardicArsonistTest extends BaseCardTest {

    @Test
    @DisplayName("Threshold grants an ETB ability that deals 3 damage to a creature")
    void thresholdDealsDamageToCreature() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player2, new EnslavedDwarf());
        var target = harness.getPermanentId(player2, "Enslaved Dwarf");
        castPardicArsonist();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Enslaved Dwarf");
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
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf(),
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf()));
        harness.setLife(player2, 20);
        castPardicArsonist();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Pardic Arsonist");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Threshold counts only the controller's graveyard")
    void thresholdDoesNotUseOpponentsGraveyard() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        harness.setLife(player2, 20);
        castPardicArsonist();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Pardic Arsonist");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Threshold becoming true after entry does not create an ETB trigger")
    void thresholdIsCheckedWhenPardicArsonistEnters() {
        harness.setGraveyard(player1, List.of(
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf(),
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf()));
        harness.setLife(player2, 20);
        castPardicArsonist();

        harness.passBothPriorities();
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A threshold ETB trigger resolves after threshold is lost")
    void thresholdTriggerIsNotRemovedWhenThresholdIsLost() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.setLife(player2, 20);
        castPardicArsonist();

        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf(),
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf()));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void castPardicArsonist() {
        harness.castFromHand(player1, new PardicArsonist(), "{2}{R}{R}");
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf(),
                new EnslavedDwarf(), new EnslavedDwarf(), new EnslavedDwarf(),
                new EnslavedDwarf());
    }
}
