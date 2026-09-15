package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({PrismaticVista.class, Forest.class, GrizzlyBears.class})
class PrismaticVistaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating pays 1 life, sacrifices the land, and searches for a basic land")
    void activationSearchesForBasicLand() {
        Forest forest = new Forest();
        activate(List.of(forest, new GrizzlyBears()));

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Prismatic Vista");
        harness.assertInGraveyard(player1, "Prismatic Vista");

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("The chosen basic land enters untapped")
    void chosenBasicLandEntersUntapped() {
        Forest forest = new Forest();
        activate(List.of(forest));

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search can fail to find a basic land")
    void canFailToFind() {
        activate(List.of(new GrizzlyBears()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }

    private void activate(List<Card> library) {
        harness.addToBattlefield(player1, new PrismaticVista());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, library);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}

@CardUsed({PrismaticVista.class, Forest.class, Island.class, Plains.class, GrizzlyBears.class})
class Mh1PrismaticVistaTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Prismatic Vista pays 1 life and sacrifices it")
    void activationPaysLifeAndSacrificesItself() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PrismaticVista());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Prismatic Vista");
        harness.assertInGraveyard(player1, "Prismatic Vista");
    }

    @Test
    @DisplayName("Resolving presents only basic lands for an untapped battlefield search")
    void resolvingPresentsBasicLandsForBattlefield() {
        activateVista();
        setupLibrary();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
    }

    @Test
    @DisplayName("The chosen basic land enters untapped")
    void chosenBasicLandEntersUntapped() {
        activateVista();
        setupLibrary();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .singleElement()
                .matches(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Prismatic Vista cannot be activated when its controller has no life to pay")
    void cannotActivateWithoutLifeToPay() {
        harness.setLife(player1, 0);
        harness.addToBattlefield(player1, new PrismaticVista());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Prismatic Vista");
    }

    private void activateVista() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PrismaticVista());
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
