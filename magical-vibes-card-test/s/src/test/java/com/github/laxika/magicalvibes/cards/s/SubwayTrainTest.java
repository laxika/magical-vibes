package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SubwayTrain.class, Forest.class, GrizzlyBears.class})
class SubwayTrainTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may pay green to search a basic land into hand")
    void entersWithBasicLandSearch() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SubwayTrain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB payment does not search")
    void mayDeclineBasicLandSearch() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new SubwayTrain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Crew 2 animates Subway Train and taps the crew")
    void crewsWithTwoPower() {
        Permanent train = addReadyTrain();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, train)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew animation ends at end of turn")
    void crewEndsAtEndOfTurn() {
        Permanent train = addReadyTrain();
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, train)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, train)).isFalse();
    }

    @Test
    @DisplayName("Paying for the search permits failing to find an available basic land")
    void mayFailToFindBasicLand() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new SubwayTrain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        var search = (PendingInteraction.LibrarySearch) gd.interaction.activeInteraction();
        assertThat(search.params().cards()).allMatch(card -> card instanceof Forest);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Paying with no basic land in the library still completes the search")
    void searchWithNoBasicLand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new SubwayTrain()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew a newly entered Vehicle")
    void summoningSickCreatureCanCrew() {
        Permanent train = harness.addToBattlefieldAndReturn(player1, new SubwayTrain());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);
        train.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, train)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, train)).isTrue();
        assertThat(train.isTapped()).isFalse();
        assertThat(train.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Tapped and opposing creatures cannot pay the crew cost")
    void cannotCrewWithUnavailableCreatures() {
        Permanent train = addReadyTrain();
        Permanent tappedCrew = addCreatureReady(player1, new GrizzlyBears());
        tappedCrew.tap();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, train)).isFalse();
        assertThat(train.isTapped()).isFalse();
    }

    private Permanent addReadyTrain() {
        Permanent train = harness.addToBattlefieldAndReturn(player1, new SubwayTrain());
        train.setSummoningSick(false);
        return train;
    }
}
