package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimalGrowth.class, Forest.class, GrizzlyBears.class})
class PrimalGrowthTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, it puts one basic land onto the battlefield")
    void putsOneBasicLandOntoBattlefieldWithoutKicker() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        castAndResolvePrimalGrowth();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(1);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Primal Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With kicker, it sacrifices a creature and puts up to two basic lands onto the battlefield")
    void sacrificesCreatureAndPutsTwoBasicLandsOntoBattlefieldWithKicker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new GrizzlyBears()));

        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Primal Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kicker requires a creature to sacrifice")
    void kickerRequiresCreatureToSacrifice() {
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Kicker cannot sacrifice a noncreature permanent")
    void kickerCannotSacrificeNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("With kicker, it may put only one basic land onto the battlefield")
    void withKickerMayPutOnlyOneBasicLandOntoBattlefield() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());

        Forest chosen = new Forest();
        Forest remaining = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, remaining, nonland));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(1)
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(remaining, nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without kicker, it may fail to find even when a basic land is available")
    void mayFindNoLandWithoutKicker() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        castAndResolvePrimalGrowth();

        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Primal Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With kicker, it may find zero lands after paying the sacrifice cost")
    void mayFindNoLandsWithKicker() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.assertInGraveyard(player1, "Primal Growth");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The two kicked lands enter together after both have been selected")
    void kickedLandsEnterSimultaneously() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();
        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not prevent the spell from resolving")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castAndResolvePrimalGrowth();

        harness.assertInGraveyard(player1, "Primal Growth");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Kicker cannot sacrifice a creature controlled by an opponent")
    void cannotSacrificeOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castAndResolvePrimalGrowth() {
        harness.setHand(player1, List.of(new PrimalGrowth()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
