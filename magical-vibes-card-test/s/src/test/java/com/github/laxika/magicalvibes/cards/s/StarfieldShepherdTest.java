package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightstallInquisitor;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Savannah;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfieldShepherd.class, Forest.class, GrizzlyBears.class, LightstallInquisitor.class,
        Memnite.class, Plains.class, Savannah.class})
class StarfieldShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("ETB searches for a basic Plains or a creature with mana value 1 or less")
    void etbSearchesForBasicPlainsOrSmallCreature() {
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Savannah(), new GrizzlyBears(), new Memnite()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Memnite");

        int memniteIndex = search.params().cards().stream()
                .map(Card::getName)
                .toList()
                .indexOf("Memnite");
        harness.handleCardChosen(player1, memniteIndex);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).contains("Memnite");
    }

    @Test
    @DisplayName("Warp casts Starfield Shepherd for {1}{W} and exiles it at the next end step")
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        StarfieldShepherd shepherd = new StarfieldShepherd();
        harness.setHand(player1, List.of(shepherd));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Starfield Shepherd");
        assertThat(gd.stack).hasSize(1);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);

        assertThat(gd.findExiledCard(shepherd.getId())).isNotNull();
    }

    @Test
    @DisplayName("ETB puts the chosen basic Plains into hand and removes only that card from the library")
    void searchesForBasicPlains() {
        Plains plains = new Plains();
        StarfieldShepherd otherShepherd = new StarfieldShepherd();
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of(plains, otherShepherd));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherShepherd);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A restricted search may fail to find even when a basic Plains is available")
    void mayFailToFind() {
        Plains plains = new Plains();
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of(plains));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A search with no eligible cards finishes without taking a card")
    void noEligibleCards() {
        StarfieldShepherd libraryCard = new StarfieldShepherd();
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Casting normally does not exile Shepherd at the end step")
    void normalCastDoesNotExile() {
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Starfield Shepherd");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with mana value exactly one can be found")
    void searchesForOneManaCreature() {
        LightstallInquisitor inquisitor = new LightstallInquisitor();
        harness.setHand(player1, List.of(new StarfieldShepherd()));
        harness.setLibrary(player1, List.of(inquisitor, new StarfieldShepherd()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(inquisitor);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(inquisitor);
    }

    @Test
    @DisplayName("A warped Shepherd can be cast from exile on a later turn and searches again")
    void castsFromExileOnLaterTurn() {
        StarfieldShepherd shepherd = new StarfieldShepherd();
        harness.setHand(player1, List.of(shepherd));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, harness::passBothPriorities);
        assertThat(gd.findExiledCard(shepherd.getId())).isNotNull();

        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));
        harness.setLibrary(player2, List.of(new Plains(), new Plains(), new Plains()));
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castFromExile(player1, shepherd.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Starfield Shepherd");
        assertThat(gd.findExiledCard(shepherd.getId())).isNull();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Starfield Shepherd");
        assertThat(gd.stack).isEmpty();
    }
}
