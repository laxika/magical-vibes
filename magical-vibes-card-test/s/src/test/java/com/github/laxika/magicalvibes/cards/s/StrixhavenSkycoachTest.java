package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrixhavenSkycoach.class, Plains.class, Forest.class, GrizzlyBears.class})
class StrixhavenSkycoachTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may search for a basic land and put it into hand")
    void etbMaySearchForBasicLand() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(plains, forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new StrixhavenSkycoach()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains, forest);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB search may be declined")
    void etbSearchMayBeDeclined() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new StrixhavenSkycoach()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Crew 2 animates Strixhaven Skycoach")
    void crewWithSufficientPower() {
        Permanent skycoach = addSkycoachReady(player1);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(skycoach.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, skycoach)).isTrue();
        assertThat(crew.isTapped()).isTrue();
        assertThat(skycoach.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent skycoach = addSkycoachReady(player1);
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, skycoach)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(skycoach.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, skycoach)).isFalse();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addSkycoachReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addSkycoachReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new StrixhavenSkycoach());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    void acceptedSearchWithEmptyLibraryFinishes() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new StrixhavenSkycoach()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWithBasicLandAvailable() {
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.setHand(player1, List.of(new StrixhavenSkycoach()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void summoningSickCreatureCanCrew() {
        Permanent skycoach = addSkycoachReady(player1);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, skycoach)).isTrue();
    }

    @Test
    void tappedCreatureAndOpposingCreatureCannotCrew() {
        addSkycoachReady(player1);
        Permanent tappedCrew = addCreatureReady(player1, new GrizzlyBears());
        tappedCrew.tap();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewedSkycoachCannotCrewItself() {
        Permanent skycoach = addSkycoachReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(skycoach.isTapped()).isFalse();
    }

    @Test
    void crewedSkycoachCannotBeBlockedByGroundCreature() {
        addSkycoachReady(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
