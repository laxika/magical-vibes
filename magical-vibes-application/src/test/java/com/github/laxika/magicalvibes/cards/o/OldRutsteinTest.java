package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OldRutstein.class, Forest.class, GrizzlyBears.class, Shock.class})
class OldRutsteinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills a land and creates a Treasure token")
    void landCreatesTreasure() {
        harness.setLibrary(player1, List.of(new Forest()));

        castAndResolve();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isZero();
        assertThat(countPermanents(player1, "Blood")).isZero();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("ETB mills a creature and creates an Insect token")
    void creatureCreatesInsect() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castAndResolve();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    @DisplayName("ETB mills a noncreature nonland card and creates a Blood token")
    void noncreatureNonlandCreatesBlood() {
        harness.setLibrary(player1, List.of(new Shock()));

        castAndResolve();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player1, "Insect")).isZero();
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("A card that is both a creature and a land creates both corresponding tokens")
    void creatureLandCreatesBothTokens() {
        Card creatureLand = new Card();
        creatureLand.setName("Dryad Arbor");
        creatureLand.setType(CardType.CREATURE);
        creatureLand.setAdditionalTypes(Set.of(CardType.LAND));
        harness.setLibrary(player1, List.of(creatureLand));

        castAndResolve();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        assertThat(countPermanents(player1, "Blood")).isZero();
    }

    @Test
    @DisplayName("At the beginning of your upkeep, Old Rutstein mills and creates the matching token")
    void upkeepTriggerMillsAndCreatesToken() {
        harness.addToBattlefield(player1, new OldRutstein());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The upkeep trigger does not fire during an opponent's upkeep")
    void upkeepTriggerDoesNotFireForOpponent() {
        harness.addToBattlefield(player1, new OldRutstein());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    private void castAndResolve() {
        harness.castFromHand(player1, new OldRutstein(), "{1}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
