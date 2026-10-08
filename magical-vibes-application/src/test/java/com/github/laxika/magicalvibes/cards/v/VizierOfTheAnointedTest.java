package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AdornedPouncer;
import com.github.laxika.magicalvibes.cards.a.AssembleFromParts;
import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrueheartDuelist;
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

@CardUsed({VizierOfTheAnointed.class, AdornedPouncer.class, DregscapeZombie.class,
        GrizzlyBears.class, TrueheartDuelist.class, AssembleFromParts.class})
class VizierOfTheAnointedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB search offers only creature cards with eternalize or embalm")
    void etbSearchOffersOnlyEternalizeOrEmbalmCreatures() {
        castVizier();
        harness.setLibrary(player1, List.of(new AdornedPouncer(), new GrizzlyBears()));

        harness.passBothPriorities(); // resolve creature spell -> MayEffect on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true); // accept -> library search inline

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Adorned Pouncer");
    }

    @Test
    @DisplayName("Choosing a searched card puts it into the graveyard")
    void chosenCardGoesToGraveyard() {
        castVizier();
        harness.setLibrary(player1, List.of(new AdornedPouncer(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Adorned Pouncer");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the may search leaves the graveyard empty")
    void decliningSearchDoesNothing() {
        castVizier();
        harness.setLibrary(player1, List.of(new AdornedPouncer(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activating an eternalize ability draws a card")
    void eternalizeActivationDrawsCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.setGraveyard(player1, List.of(new AdornedPouncer()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0); // Eternalize {3}{W}{W}
        harness.passBothPriorities(); // resolve the draw trigger

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activating a non-eternalize/embalm graveyard ability does not draw")
    void nonEternalizeGraveyardAbilityDoesNotDraw() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.setGraveyard(player1, List.of(new DregscapeZombie()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0); // Unearth {B}
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's eternalize activation does not draw for the Vizier's controller")
    void opponentEternalizeDoesNotDraw() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.setGraveyard(player2, List.of(new AdornedPouncer()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB can find an embalm creature and put it into the graveyard")
    void searchFindsEmbalmCreature() {
        castVizier();
        Card duelist = new TrueheartDuelist();
        harness.setLibrary(player1, List.of(duelist, new VizierOfTheAnointed()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(duelist);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(duelist);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(duelist).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted library search may fail to find even with a matching card")
    void searchMayFailToFind() {
        castVizier();
        Card pouncer = new AdornedPouncer();
        harness.setLibrary(player1, List.of(pouncer));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pouncer);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Embalm draws after paying its costs and before creating its token")
    void embalmDrawsBeforeTokenIsCreated() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        Card duelist = new TrueheartDuelist();
        Card draw = new VizierOfTheAnointed();
        harness.setGraveyard(player1, List.of(duelist));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(duelist);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Trueheart Duelist");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        harness.assertNotOnBattlefield(player1, "Trueheart Duelist");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Trueheart Duelist");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    @DisplayName("Each Vizier draws once for the same eternalize activation")
    void multipleViziersEachDraw() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        harness.setGraveyard(player1, List.of(new AdornedPouncer()));
        Card first = new VizierOfTheAnointed();
        Card second = new AdornedPouncer();
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        harness.assertNotOnBattlefield(player1, "Adorned Pouncer");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Adorned Pouncer");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("The token-copy ability granted by Assemble from Parts is not embalm or eternalize")
    void otherGraveyardTokenCopyAbilityDoesNotTriggerDraw() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card creature = new VizierOfTheAnointed();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AssembleFromParts()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.addToBattlefield(player1, new VizierOfTheAnointed());
        Card draw = new AdornedPouncer();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(draw);
    }

    private void castVizier() {
        harness.castFromHand(player1, new VizierOfTheAnointed(), "{3}{U}");
    }
}
