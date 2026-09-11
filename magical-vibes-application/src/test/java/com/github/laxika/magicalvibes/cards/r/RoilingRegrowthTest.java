package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoilingRegrowth.class, Mountain.class, Plains.class, Forest.class, GrizzlyBears.class})
class RoilingRegrowthTest extends BaseCardTest {

    @Test
    @DisplayName("The land is sacrificed when Roiling Regrowth resolves")
    void sacrificesLandOnResolution() {
        Permanent land = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(land);
        castRoilingRegrowth();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Resolving offers up to two basic lands for the tapped battlefield")
    void offersBasicLandsTapped() {
        castRoilingRegrowth();
        List<Card> library = setUpLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrderElementsOf(library.subList(0, 2));
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
    }

    @Test
    @DisplayName("Both selected basic lands enter tapped")
    void selectedBasicLandsEnterTapped() {
        castRoilingRegrowth();
        setUpLibrary();

        harness.passBothPriorities();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .filteredOn(Permanent::isTapped)
                .hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The player may fail to find basic lands")
    void canFailToFind() {
        castRoilingRegrowth();
        setUpLibrary();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Plains")
                        || permanent.getCard().getName().equals("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRoilingRegrowth() {
        harness.setHand(player1, List.of(new RoilingRegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
    }

    private List<Card> setUpLibrary() {
        Card plains = new Plains();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(plains, forest, new GrizzlyBears()));
        return List.of(plains, forest);
    }
}
