package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViewpointSynchronization.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class ViewpointSynchronizationTest extends BaseCardTest {

    @Test
    void putsTwoBasicLandsTappedAndTheThirdIntoHand() {
        setupAndCast(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isTapped)
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1)
                .anyMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    void withTwoOrFewerFoundLandsAllEnterTheBattlefield() {
        setupAndCast(List.of(new Forest(), new Island(), new GrizzlyBears()));

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isTapped)
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2);
    }

    private void setupAndCast(List<com.github.laxika.magicalvibes.model.Card> library) {
        harness.setHand(player1, List.of(new ViewpointSynchronization()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, 0);
    }
}
