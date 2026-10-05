package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AutonSoldier;
import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.c.ClockworkDroid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Stomp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntoTheTimeVortex.class, Mountain.class, Forest.class, AutonSoldier.class, ClockworkDroid.class})
class IntoTheTimeVortexTest extends BaseCardTest {

    @Test
    void cascadeCastsFirstCheaperNonland() {
        Mountain skippedLand = new Mountain();
        AutonSoldier skippedNonland = new AutonSoldier();
        ClockworkDroid hit = new ClockworkDroid();
        Forest belowHit = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, skippedNonland, hit, belowHit));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new IntoTheTimeVortex(), "{4}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Clockwork Droid");

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, skippedLand, skippedNonland);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        IntoTheTimeVortex card = new IntoTheTimeVortex();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, card, "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Into the Time Vortex");
    }

    @Test
    void cascadeSkipsEqualManaValueAndStopsAtFirstCheaperCard() {
        IntoTheTimeVortex equalManaValue = new IntoTheTimeVortex();
        ClockworkDroid firstHit = new ClockworkDroid();
        ClockworkDroid laterHit = new ClockworkDroid();
        harness.setLibrary(player1, List.of(equalManaValue, firstHit, laterHit));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new IntoTheTimeVortex(), "{4}{R}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(firstHit);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(laterHit, equalManaValue);
        assertThat(gd.findExiledCard(equalManaValue.getId())).isNull();
        assertThat(gd.findExiledCard(firstHit.getId())).isNull();
    }

    @Test
    void decliningCascadeReturnsEveryExiledCardBelowUntouchedLibrary() {
        Mountain skippedLand = new Mountain();
        ClockworkDroid hit = new ClockworkDroid();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(skippedLand, hit, untouched));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new IntoTheTimeVortex(), "{4}{R}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 3))
                .containsExactlyInAnyOrder(skippedLand, hit);
        assertThat(gd.findExiledCard(skippedLand.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
    }

    @Test
    void reboundCastCascadesAgainAndThenGoesToGraveyard() {
        IntoTheTimeVortex card = new IntoTheTimeVortex();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, card, "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        ClockworkDroid hit = new ClockworkDroid();
        harness.setLibrary(player1, List.of(hit, new Forest()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(hit);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Clockwork Droid");
        harness.assertInGraveyard(player1, "Into the Time Vortex");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void decliningReboundLeavesCardExiledWithoutAnotherOffer() {
        IntoTheTimeVortex card = new IntoTheTimeVortex();
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, card, "{4}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Into the Time Vortex");

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    @CardUsed({BonecrusherGiant.class, Stomp.class})
    void cascadeOffersBothLegalAdventureAndCreatureSpells() {
        BonecrusherGiant hit = new BonecrusherGiant();
        harness.setLibrary(player1, List.of(hit, new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new IntoTheTimeVortex(), "{4}{R}");
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains("Cast Bonecrusher Giant", "Cast Stomp");
    }
}
