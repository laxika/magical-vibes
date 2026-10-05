package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TeferiMageOfZhalfir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindsDilation.class, CounselOfTheSoratami.class, Forest.class, GrizzlyBears.class})
class MindsDilationTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's first spell exiles the top card and offers a free cast")
    void firstOpponentSpellExilesTopCardAndOffersFreeCast() {
        harness.addToBattlefield(player1, new MindsDilation());
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));

        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(exiledCard);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == exiledCard);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining a nonland card leaves it in exile")
    void decliningNonlandCardLeavesItInExile() {
        harness.addToBattlefield(player1, new MindsDilation());
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));

        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
    }

    @Test
    @DisplayName("A land exiled by the trigger remains in exile without a cast choice")
    void landIsExiledWithoutCastChoice() {
        harness.addToBattlefield(player1, new MindsDilation());
        Forest exiledLand = new Forest();
        harness.setLibrary(player2, List.of(exiledLand));

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledLand);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Only each opponent's first spell of the turn triggers")
    void onlyFirstSpellOfTurnTriggers() {
        harness.addToBattlefield(player1, new MindsDilation());
        Forest exiledLand = new Forest();
        harness.setLibrary(player2, List.of(exiledLand));

        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        int librarySizeAfterFirstSpell = gd.playerDecks.get(player2.getId()).size();
        harness.castCreature(player2, 0);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeAfterFirstSpell);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The controller's spells do not trigger Mind's Dilation")
    void controllerSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MindsDilation());
        Forest topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library produces no exile or free cast")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new MindsDilation());
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A first spell cast before Mind's Dilation entered still counts")
    void firstSpellBeforeEnchantmentEnteredStillCounts() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new MindsDilation());
        Forest topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The top card is determined when the trigger resolves")
    void exilesCurrentTopCardOnResolution() {
        harness.addToBattlefield(player1, new MindsDilation());
        GrizzlyBears originalTop = new GrizzlyBears();
        Forest newTop = new Forest();
        harness.setLibrary(player2, List.of(originalTop));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.setLibrary(player2, List.of(newTop, originalTop));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(newTop);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({TeferiMageOfZhalfir.class})
    @DisplayName("Teferi's casting restriction prevents the free cast")
    void opponentTeferiPreventsFreeCast() {
        harness.addToBattlefield(player1, new MindsDilation());
        harness.addToBattlefield(player2, new TeferiMageOfZhalfir());
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledCard);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == exiledCard);
    }

    @Test
    @DisplayName("A free sorcery resolves before the opponent's spell and goes to its owner's graveyard")
    void freeSorceryResolvesFirstAndGoesToOwnersGraveyard() {
        harness.addToBattlefield(player1, new MindsDilation());
        CounselOfTheSoratami exiledCard = new CounselOfTheSoratami();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setLibrary(player2, List.of(exiledCard));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(exiledCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(exiledCard);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }
}
