package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RangerCaptainOfEos.class, Disenchant.class, GrizzlyBears.class, Memnite.class, Shock.class})
class RangerCaptainOfEosTest extends BaseCardTest {

    @Test
    void acceptsEtbTutorForOneManaCreature() {
        Card oneManaCreature = new Memnite();
        harness.setLibrary(player1, List.of(oneManaCreature, new GrizzlyBears(), new Disenchant()));

        castRangerCaptain();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(oneManaCreature);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Memnite");
    }

    @Test
    void decliningEtbTutorDoesNothing() {
        harness.setLibrary(player1, List.of(new Memnite()));

        castRangerCaptain();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Memnite");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void sacrificePreventsOpponentsFromCastingNoncreatureSpellsThisTurn() {
        addCreatureReady(player1, new RangerCaptainOfEos());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Ranger-Captain of Eos");
    }

    @Test
    void sacrificeStillAllowsControllerToCastNoncreatureSpells() {
        addCreatureReady(player1, new RangerCaptainOfEos());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void sacrificeStillAllowsOpponentsToCastCreatureSpells() {
        addCreatureReady(player1, new RangerCaptainOfEos());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The enters-the-battlefield ability searches for a creature with mana value 1 or less")
    void searchesForLowManaValueCreature() {
        harness.setHand(player1, List.of(new RangerCaptainOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(new Memnite(), new GrizzlyBears(), new Shock()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Memnite");

        harness.getGameService().handleInteractionAnswer(gameData, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Memnite");
    }

    private void castRangerCaptain() {
        harness.setHand(player1, List.of(new RangerCaptainOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }
}
