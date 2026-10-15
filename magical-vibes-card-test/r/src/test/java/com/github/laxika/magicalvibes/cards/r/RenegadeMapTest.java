package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RenegadeMap.class, Plains.class, Forest.class, Island.class, DruidOfTheCowl.class})
class RenegadeMapTest extends BaseCardTest {

    @Test
    @DisplayName("Renegade Map enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new RenegadeMap()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent map = findPermanent(player1, "Renegade Map");
        assertThat(map.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating Renegade Map sacrifices it and searches for a basic land")
    void activatingSacrificesAndSearches() {
        harness.addToBattlefield(player1, new RenegadeMap());
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new DruidOfTheCowl()));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Renegade Map");
        harness.assertInGraveyard(player1, "Renegade Map");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(3)
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
    }

    @Test
    @DisplayName("Choosing a basic land from Renegade Map's search puts it into hand")
    void chosenBasicLandEntersHand() {
        harness.addToBattlefield(player1, new RenegadeMap());
        harness.setLibrary(player1, List.of(new Plains(), new DruidOfTheCowl()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).singleElement().isInstanceOf(DruidOfTheCowl.class);
        assertThat(gameLogContains("reveals Plains")).isTrue();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void tappedMapCannotBeActivated() {
        Permanent map = harness.addToBattlefieldAndReturn(player1, new RenegadeMap());
        map.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Renegade Map");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canFailToFindEvenWithBasicLandAvailable() {
        harness.addToBattlefield(player1, new RenegadeMap());
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        harness.assertInGraveyard(player1, "Renegade Map");
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void libraryWithoutBasicLandsStillShuffles() {
        harness.addToBattlefield(player1, new RenegadeMap());
        Card creature = new DruidOfTheCowl();
        harness.setLibrary(player1, List.of(creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotInHand(player1, "Druid of the Cowl");
        harness.assertInGraveyard(player1, "Renegade Map");
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }

    @Test
    void emptyLibraryDoesNotPreventActivation() {
        harness.addToBattlefield(player1, new RenegadeMap());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Renegade Map");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("Library is shuffled.")).isTrue();
    }
}
