package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DesertOfTheIndomitable;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FrilledSandwalla;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.ValakutTheMoltenPinnacle;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HourOfPromise.class, Forest.class, Plains.class, DesertOfTheIndomitable.class,
        FrilledSandwalla.class, Mountain.class, ValakutTheMoltenPinnacle.class})
class HourOfPromiseTest extends BaseCardTest {

    private PendingInteraction.LibrarySearch activeSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void castHourOfPromise() {
        harness.castFromHand(player1, new HourOfPromise(), "{4}{G}");
    }

    @Test
    @DisplayName("Resolving offers up to two land cards to the battlefield tapped")
    void resolvesOffersUpToTwoLandsToBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new FrilledSandwalla()));
        castHourOfPromise();

        harness.passBothPriorities();

        assertThat(activeSearch()).isNotNull();
        assertThat(activeSearch().params().remainingCount()).isEqualTo(2);
        assertThat(activeSearch().params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(activeSearch().params().canFailToFind()).isTrue();
        assertThat(activeSearch().params().cards())
                .allMatch(c -> c.hasType(CardType.LAND))
                .noneMatch(c -> c.getName().equals("Frilled Sandwalla"));
    }

    @Test
    @DisplayName("Chosen lands enter the battlefield tapped")
    void chosenLandsEnterTapped() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new FrilledSandwalla()));
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(p -> p.isTapped());
        harness.assertInGraveyard(player1, "Hour of Promise");
    }

    @Test
    @DisplayName("Fetched Deserts count toward creating two Zombie tokens")
    void fetchedDesertsCountTowardZombies() {
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        harness.setLibrary(player1, List.of(new DesertOfTheIndomitable(), new DesertOfTheIndomitable(), new FrilledSandwalla()));
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE))
                .hasSize(2)
                .allMatch(p -> p.getEffectivePower() == 2 && p.getEffectiveToughness() == 2);
    }

    @Test
    @DisplayName("Without three Deserts, no Zombie tokens are created")
    void fewerThanThreeDesertsCreatesNoZombies() {
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new FrilledSandwalla()));
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.ZOMBIE));
    }

    @Test
    @DisplayName("Finding only two Deserts does not create Zombies")
    void twoFetchedDesertsAreNotEnough() {
        harness.setLibrary(player1, List.of(new DesertOfTheIndomitable(), new DesertOfTheIndomitable()));
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
        harness.assertInGraveyard(player1, "Hour of Promise");
    }

    @Test
    @DisplayName("Choosing no lands still creates Zombies with three Deserts")
    void decliningSearchStillCreatesZombies() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        }
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertZombiesCreated();
        harness.assertInGraveyard(player1, "Hour of Promise");
    }

    @Test
    @DisplayName("Choosing one Desert and declining the second land still checks the new Desert count")
    void choosingOnlyOneLandCanReachThreeDeserts() {
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        harness.setLibrary(player1, List.of(new DesertOfTheIndomitable(), new Forest()));
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(activeSearch()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .hasSize(3);
        assertZombiesCreated();
    }

    @Test
    @DisplayName("An empty library does not prevent Zombie creation")
    void emptyLibraryStillCreatesZombies() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        }
        harness.setLibrary(player1, List.of());
        castHourOfPromise();

        harness.passBothPriorities();

        assertThat(activeSearch()).isNull();
        assertZombiesCreated();
        harness.assertInGraveyard(player1, "Hour of Promise");
    }

    @Test
    @DisplayName("The opponent's Deserts do not count")
    void opponentsDesertsDoNotCount() {
        harness.addToBattlefield(player1, new DesertOfTheIndomitable());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new DesertOfTheIndomitable());
        }
        harness.setLibrary(player1, List.of(new DesertOfTheIndomitable(), new Forest()));
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A fetched Valakut sees the Mountain entering alongside it regardless of pick order")
    void fetchedLandsEnterSimultaneously() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setLibrary(player1, List.of(new Mountain(), new ValakutTheMoltenPinnacle()));
        harness.setLife(player2, 20);
        castHourOfPromise();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Valakut, the Molten Pinnacle"))
                .allMatch(p -> p.isTapped());
        harness.assertInGraveyard(player1, "Hour of Promise");
    }

    private void assertZombiesCreated() {
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(p.getEffectivePower()).isEqualTo(2);
                    assertThat(p.getEffectiveToughness()).isEqualTo(2);
                    assertThat(p.isTapped()).isFalse();
                });
    }
}
