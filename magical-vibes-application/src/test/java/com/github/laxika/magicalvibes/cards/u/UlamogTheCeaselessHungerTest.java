package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GideonAllyOfZendikar;
import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlamogTheCeaselessHunger.class, ExpeditionEnvoy.class,
        GideonAllyOfZendikar.class, SmiteTheMonstrous.class})
class UlamogTheCeaselessHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Indestructible prevents a destroy spell from removing Ulamog")
    void survivesDestroySpell() {
        Permanent ulamog = harness.addToBattlefieldAndReturn(player2, new UlamogTheCeaselessHunger());
        harness.setHand(player1, List.of(new SmiteTheMonstrous()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, ulamog.getId());

        harness.assertOnBattlefield(player2, "Ulamog, the Ceaseless Hunger");
        harness.assertNotInGraveyard(player2, "Ulamog, the Ceaseless Hunger");
    }

    @Test
    @DisplayName("Ulamog can be cast with fewer than two legal targets without exiling anything")
    void insufficientTargetsDoNotPreventCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        harness.setHand(player1, List.of(new UlamogTheCeaselessHunger()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ulamog, the Ceaseless Hunger");
        harness.assertOnBattlefield(player2, "Expedition Envoy");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
    }

    @Test
    @DisplayName("Putting Ulamog onto the battlefield does not trigger its cast ability")
    void enteringWithoutCastingDoesNotExilePermanents() {
        harness.addToBattlefield(player2, new ExpeditionEnvoy());
        harness.addToBattlefield(player2, new ExpeditionEnvoy());

        harness.enterBattlefieldAndReturn(player1, new UlamogTheCeaselessHunger());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Expedition Envoy")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger exiles all remaining cards from a library smaller than twenty")
    void attackingExilesShortLibrary() {
        addCreatureReady(player1, new UlamogTheCeaselessHunger());
        List<Card> library = List.of(new ExpeditionEnvoy(), new ExpeditionEnvoy());
        harness.setLibrary(player2, library);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(library.stream().map(Card::getId).toList());
    }

    @Test
    @DisplayName("The defending player still exiles cards after the attacked planeswalker leaves")
    void attackTriggerRemembersDefenderAfterPlaneswalkerLeaves() {
        addCreatureReady(player1, new UlamogTheCeaselessHunger());
        Permanent gideon = harness.addToBattlefieldAndReturn(player2, new GideonAllyOfZendikar());
        List<Card> library = List.of(new ExpeditionEnvoy(), new ExpeditionEnvoy());
        harness.setLibrary(player2, library);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, gideon.getId()));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(gideon);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(library.stream().map(Card::getId).toList());
    }

    @Test
    @DisplayName("Casting Ulamog exiles two target permanents")
    void castingExilesTwoTargetPermanents() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new ExpeditionEnvoy());
        harness.setHand(player1, List.of(new UlamogTheCeaselessHunger()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(firstTarget.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(secondTarget.getOriginalCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player2, "Expedition Envoy");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Ulamog, the Ceaseless Hunger");
    }

    @Test
    @DisplayName("Attacking Ulamog exiles the top twenty cards of the defending player's library")
    void attackingExilesTopTwentyCardsOfDefendingLibrary() {
        Permanent ulamog = addCreatureReady(player1, new UlamogTheCeaselessHunger());
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            library.add(new ExpeditionEnvoy());
        }
        harness.setLibrary(player2, library);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ulamog)));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .containsExactlyElementsOf(library.subList(20, 25));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(library.subList(0, 20).stream().map(Card::getId).toList());
    }
}
