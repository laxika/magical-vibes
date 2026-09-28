package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProcessingPlant.class, Forest.class, PathToExile.class})
class ProcessingPlantTest extends BaseCardTest {

    @Test
    void entersTheBattlefieldTapped() {
        harness.setHand(player1, List.of(new ProcessingPlant()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Processing Plant").isTapped()).isTrue();
    }

    @Test
    void processesOpponentOwnedExiledCardAndUntaps() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new ProcessingPlant()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.OpponentOwnedExiledCardToGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCard.getId()));

        Permanent plant = findPermanent(player1, "Processing Plant");
        assertThat(plant.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Path to Exile");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
    }

    @Test
    void decliningProcessingExilesTopCardOfEachOpponentLibrary() {
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        Forest topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new ProcessingPlant()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(findPermanent(player1, "Processing Plant").isTapped()).isTrue();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    void addsOneManaOfEachListedColorOrColorless() {
        harness.addToBattlefield(player1, new ProcessingPlant());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.COLORLESS.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
