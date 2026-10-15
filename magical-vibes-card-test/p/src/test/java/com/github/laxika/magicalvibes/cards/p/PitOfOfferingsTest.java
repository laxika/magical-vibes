package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CavernStomper;
import com.github.laxika.magicalvibes.cards.w.WaterwindScout;
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

@CardUsed({PitOfOfferings.class, CavernStomper.class, WaterwindScout.class})
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
        Card green = new CavernStomper();
        Card blue = new WaterwindScout();
        Card opponentGreen = new CavernStomper();
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
        Card green = new CavernStomper();
        Card blue = new WaterwindScout();
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

    @Test
    void canChooseZeroTargetsWithCardsInGraveyards() {
        Card green = new CavernStomper();
        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(new PitOfOfferings()));
        harness.playLand(player1, 0);

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        Permanent pit = findPermanent(player1, "Pit of Offerings");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(green);
        assertThat(gd.getCardsExiledByPermanent(pit.getId())).isEmpty();
        pit.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(pit.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void addsColorlessManaWithEmptyGraveyards() {
        harness.setHand(player1, List.of(new PitOfOfferings()));
        harness.playLand(player1, 0);
        resolveAllTriggers();
        Permanent pit = findPermanent(player1, "Pit of Offerings");
        pit.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(pit.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void automaticallyAddsTheOnlyAvailableColor() {
        Permanent pit = playPitAndChoose(new CavernStomper());
        pit.untap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(pit.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesRemainingLegalTargetsWhenOneLeavesTheGraveyard() {
        Card green = new CavernStomper();
        Card blue = new WaterwindScout();
        harness.setGraveyard(player1, List.of(green, blue));
        harness.setHand(player1, List.of(new PitOfOfferings()));
        harness.playLand(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(green.getId(), blue.getId()));

        harness.setGraveyard(player1, List.of(green));
        harness.setHand(player1, List.of(blue));
        resolveAllTriggers();

        Permanent pit = findPermanent(player1, "Pit of Offerings");
        assertThat(gd.getCardsExiledByPermanent(pit.getId())).containsExactly(green);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(blue);
        pit.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void stopsUsingAColorWhenItsCardLeavesExile() {
        Card green = new CavernStomper();
        Card blue = new WaterwindScout();
        Permanent pit = playPitAndChoose(green, blue);
        gd.removeFromExile(blue.getId());
        harness.setHand(player1, List.of(blue));
        pit.untap();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
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
