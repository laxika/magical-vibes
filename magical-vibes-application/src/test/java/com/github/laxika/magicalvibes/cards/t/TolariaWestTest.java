package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolariaWest.class, DryadArbor.class, Tarmogoyf.class})
class TolariaWestTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new TolariaWest()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Tolaria West").isTapped()).isTrue();
    }

    @Test
    void tappingProducesBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TolariaWest());
        land.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void transmuteSearchesForManaValueZeroCard() {
        DryadArbor matchingCard = new DryadArbor();
        Tarmogoyf nonMatchingCard = new Tarmogoyf();
        harness.setHand(player1, List.of(new TolariaWest()));
        harness.setLibrary(player1, List.of(matchingCard, nonMatchingCard));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(matchingCard);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Tolaria West");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(matchingCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonMatchingCard);
    }

    @Test
    void transmuteCanOnlyBeActivatedAtSorcerySpeed() {
        TolariaWest land = new TolariaWest();
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
