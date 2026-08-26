package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.i.IslandFishJasconius;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PitOfOfferings.class, ForestBear.class, IslandFishJasconius.class})
class PitOfOfferingsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new PitOfOfferings()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Pit of Offerings").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles up to three cards from any graveyard and tracks them with the land")
    void exilesCardsFromAnyGraveyardWithSourceTracking() {
        Card green = new ForestBear();
        Card blue = new IslandFishJasconius();
        Card opponentGreen = new ForestBear();
        harness.setGraveyard(player1, List.of(green, blue));
        harness.setGraveyard(player2, List.of(opponentGreen));
        harness.setHand(player1, List.of(new PitOfOfferings()));

        harness.playLand(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                green.getId(), blue.getId(), opponentGreen.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultipleCardsChosen(player1,
                List.of(green.getId(), blue.getId(), opponentGreen.getId()));
        resolveAllTriggers();

        Permanent pit = findPermanent(player1, "Pit of Offerings");
        assertThat(gd.getCardsExiledByPermanent(pit.getId()))
                .containsExactlyInAnyOrder(green, blue, opponentGreen);
    }

    @Test
    @DisplayName("Adds mana only of a color represented by cards it exiled")
    void addsManaOfAnExiledCardColor() {
        Card green = new ForestBear();
        Card blue = new IslandFishJasconius();
        Permanent pit = playPitAndChoose(green, blue);
        pit.untap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLUE", "GREEN");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Produces no colored mana when its exiled cards are all colorless")
    void producesNoManaWhenNoExiledCardHasAColor() {
        Card colorless = new PitOfOfferings();
        Permanent pit = playPitAndChoose(colorless);
        pit.untap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent playPitAndChoose(Card... cards) {
        harness.setGraveyard(player1, Arrays.asList(cards));
        harness.setHand(player1, List.of(new PitOfOfferings()));
        harness.playLand(player1, 0);

        harness.handleMultipleCardsChosen(player1,
                Arrays.stream(cards).map(Card::getId).toList());
        resolveAllTriggers();
        return findPermanent(player1, "Pit of Offerings");
    }
}
