package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsumingAberration.class, Divination.class, Forest.class, GrizzlyBears.class})
class ConsumingAberrationTest extends BaseCardTest {

    private Permanent aberration() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Consuming Aberration".equals(p.getCard().getName()))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Power and toughness equal the number of cards in opponents' graveyards")
    void powerToughnessMatchesOpponentGraveyards() {
        harness.addToBattlefield(player1, new ConsumingAberration());

        assertThat(gqs.getEffectivePower(gd, aberration())).isZero();
        assertThat(gqs.getEffectiveToughness(gd, aberration())).isZero();

        harness.setGraveyard(player2, List.of(new Forest(), new GrizzlyBears(), new Divination()));

        assertThat(gqs.getEffectivePower(gd, aberration())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aberration())).isEqualTo(3);
    }

    @Test
    @DisplayName("Cards in the controller's own graveyard do not count")
    void ownGraveyardDoesNotCount() {
        harness.addToBattlefield(player1, new ConsumingAberration());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, aberration())).isZero();
    }

    @Test
    @DisplayName("Casting a spell makes each opponent mill until they reveal a land")
    void spellCastMillsOpponentUntilLand() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new ConsumingAberration());

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new Divination(),
                new Forest(),      // land -> stop
                new GrizzlyBears() // stays in library
        ));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting("name")
                .containsExactlyInAnyOrder("Grizzly Bears", "Divination", "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("The mill trigger grows the Aberration, since the milled cards land in an opponent's graveyard")
    void millFeedsItsOwnPowerToughness() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new ConsumingAberration());
        // Keeps it above 0/0 so state-based actions don't bin it before the trigger resolves.
        harness.setGraveyard(player2, List.of(new Divination()));

        harness.setLibrary(player2, List.of(
                new GrizzlyBears(),
                new Forest()
        ));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aberration())).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aberration())).isEqualTo(3);
    }

    @Test
    @DisplayName("A library without lands is put entirely into the graveyard")
    void libraryWithoutLandsIsEmptied() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new ConsumingAberration());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Divination()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name")
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears", "Divination");
        assertThat(gd.playerDecks.get(player1.getId())).extracting("name").containsExactly("Forest");
        assertThat(gqs.getEffectivePower(gd, aberration())).isEqualTo(3);
    }

    @Test
    @DisplayName("A land on top is included and stops further reveals")
    void landOnTopStopsRevealing() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player2, List.of(new Divination()));
        harness.addToBattlefield(player1, new ConsumingAberration());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears()));

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name")
                .containsExactlyInAnyOrder("Divination", "Forest");
        assertThat(gd.playerDecks.get(player2.getId())).extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent casting a spell does not trigger the ability")
    void opponentSpellDoesNotTrigger() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new ConsumingAberration());
        harness.setLibrary(player2, List.of(new Divination(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).extracting("name")
                .containsExactly("Divination", "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting("name")
                .containsExactly("Grizzly Bears", "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty library causes no reveals or graveyard changes")
    void emptyLibraryDoesNothing() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new ConsumingAberration());
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name").containsExactly("Forest");
        harness.assertOnBattlefield(player1, "Consuming Aberration");
    }

    @Test
    @DisplayName("Noncreature spells trigger the ability before the spell resolves")
    void sorceryTriggersBeforeResolving() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new ConsumingAberration());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name")
                .containsExactlyInAnyOrder("Forest", "Grizzly Bears", "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Removing the last opposing graveyard card reduces toughness to zero")
    void emptyingOpposingGraveyardKillsAberration() {
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new ConsumingAberration());
        assertThat(gqs.getEffectiveToughness(gd, aberration())).isEqualTo(1);

        harness.setGraveyard(player2, List.of());
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Consuming Aberration");
        harness.assertInGraveyard(player1, "Consuming Aberration");
    }
}
