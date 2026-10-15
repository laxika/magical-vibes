package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NarsetsReversal;
import com.github.laxika.magicalvibes.cards.w.WreckingBeast;
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

@CardUsed({EmergencyPowers.class, Forest.class, AxebaneBeast.class, WreckingBeast.class,
        EndRazeForerunners.class, NarsetsReversal.class})
class EmergencyPowersTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles hand and graveyard into their library and draws seven cards")
    void shufflesHandsAndGraveyardsAndDrawsSeven() {
        forceMainPhase();
        EmergencyPowers emergencyPowers = new EmergencyPowers();
        harness.setHand(player1, List.of(emergencyPowers));
        harness.setGraveyard(player1, List.of(new EmergencyPowers()));
        harness.setLibrary(player1, spells(6));
        harness.setLibrary(player2, forests(7));
        addEmergencyPowersMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(emergencyPowers.getId());
    }

    @Test
    @DisplayName("During your main phase, addendum may put one eligible permanent from hand onto the battlefield")
    void addendumPutsEligiblePermanentOntoBattlefield() {
        forceMainPhase();
        EmergencyPowers emergencyPowers = new EmergencyPowers();
        AxebaneBeast beast = new AxebaneBeast();
        EmergencyPowers otherSpell = new EmergencyPowers();
        harness.setHand(player1, List.of(emergencyPowers, beast, otherSpell));
        harness.setLibrary(player1, forests(5));
        harness.setLibrary(player2, forests(7));
        addEmergencyPowersMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).contains(beast.getId()).doesNotContain(otherSpell.getId());
        harness.handleMultipleCardsChosen(player1, List.of(beast.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(beast.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(beast);
    }

    @Test
    @DisplayName("Addendum does not apply when Emergency Powers is cast outside a main phase")
    void addendumDoesNotApplyOutsideMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        EmergencyPowers emergencyPowers = new EmergencyPowers();
        AxebaneBeast beast = new AxebaneBeast();
        harness.setHand(player1, List.of(emergencyPowers, beast));
        harness.setLibrary(player1, forests(6));
        harness.setLibrary(player2, forests(7));
        addEmergencyPowersMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(beast.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(beast);
    }

    @Test
    void exilesBeforeOfferingAddendum() {
        forceMainPhase();
        EmergencyPowers spell = new EmergencyPowers();
        harness.setLibrary(player1, forests(7));
        harness.setLibrary(player2, forests(7));
        harness.castFromHand(player1, spell, "{5}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    void copiedSpellDoesNotGetAddendumDuringControllersMainPhase() {
        forceMainPhase();
        EmergencyPowers spell = new EmergencyPowers();
        harness.setHand(player1, List.of(spell, new NarsetsReversal()));
        harness.setLibrary(player1, forests(5));
        harness.setLibrary(player2, forests(7));
        addEmergencyPowersMana();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void addendumIncludesManaValueSevenAndLandsButExcludesManaValueEight() {
        forceMainPhase();
        WreckingBeast seven = new WreckingBeast();
        EndRazeForerunners eight = new EndRazeForerunners();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(seven, eight, land,
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, forests(7));
        harness.castFromHand(player1, new EmergencyPowers(), "{5}{W}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        assertThat(choice.validCardIds()).contains(seven.getId(), land.getId()).doesNotContain(eight.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).contains(seven, eight).doesNotContain(land);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    void shufflesBothPlayersHandsAndGraveyardsWithoutMovingBattlefieldCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        AxebaneBeast creature = new AxebaneBeast();
        harness.addToBattlefield(player1, creature);
        Forest ownHand = new Forest();
        Forest ownGraveyard = new Forest();
        Forest opponentHand = new Forest();
        Forest opponentGraveyard = new Forest();
        harness.setHand(player1, List.of(new EmergencyPowers(), ownHand));
        harness.setGraveyard(player1, List.of(ownGraveyard));
        harness.setLibrary(player1, forests(5));
        harness.setHand(player2, List.of(opponentHand));
        harness.setGraveyard(player2, List.of(opponentGraveyard));
        harness.setLibrary(player2, forests(5));
        addEmergencyPowersMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7).contains(ownHand, ownGraveyard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7).contains(opponentHand, opponentGraveyard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Axebane Beast");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void addendumAppliesDuringPostcombatMainPhaseAndCanBeDeclined() {
        forceMainPhase();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setLibrary(player1, forests(7));
        harness.setLibrary(player2, forests(7));
        harness.castFromHand(player1, new EmergencyPowers(), "{5}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addEmergencyPowersMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private List<Card> forests(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new Forest())
                .toList();
    }

    private List<Card> spells(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new EmergencyPowers())
                .toList();
    }
}
