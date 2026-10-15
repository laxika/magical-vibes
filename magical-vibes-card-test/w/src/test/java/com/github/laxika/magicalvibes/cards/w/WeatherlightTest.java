package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.j.JoustingLance;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Weatherlight.class, ArvadTheCursed.class, GrizzlyBears.class, SerraAngel.class,
        Shock.class, HistoryOfBenalia.class, JoustingLance.class})
class WeatherlightTest extends BaseCardTest {

    @Test
    @DisplayName("Weatherlight is not a creature before crewing")
    void notACreatureBeforeCrew() {
        Permanent weatherlight = addWeatherlightReady(player1);

        assertThat(gqs.isCreature(gd, weatherlight)).isFalse();
    }

    @Test
    @DisplayName("Crewing with a single creature of sufficient power animates Weatherlight")
    void crewWithSingleCreature() {
        Permanent weatherlight = addWeatherlightReady(player1);
        Permanent crew = addCreatureReady(player1, new SerraAngel()); // 4/4, power >= 3

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weatherlight.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, weatherlight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, weatherlight)).isEqualTo(5);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crewing with multiple small creatures (total power >= 3) works")
    void crewWithMultipleCreatures() {
        Permanent weatherlight = addWeatherlightReady(player1);
        // Two 2/2 creatures — total power 4 >= 3
        Permanent bear1 = addCreatureReady(player1, new GrizzlyBears());
        Permanent bear2 = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weatherlight.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(bear1.isTapped()).isTrue();
        assertThat(bear2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot crew without enough creature power")
    void cannotCrewWithoutEnoughPower() {
        addWeatherlightReady(player1);
        // Single 2/2 creature — power 2 < 3
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Cannot crew when no creatures are available")
    void cannotCrewWithNoCreatures() {
        addWeatherlightReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    @DisplayName("Crew animation resets at end of turn")
    void crewResetsAtEndOfTurn() {
        Permanent weatherlight = addWeatherlightReady(player1);
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weatherlight.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, weatherlight)).isFalse();
    }

    @Test
    @DisplayName("Crew does not require the vehicle to tap")
    void crewDoesNotTapVehicle() {
        Permanent weatherlight = addWeatherlightReady(player1);
        addCreatureReady(player1, new SerraAngel());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(weatherlight.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures cannot be used to crew")
    void tappedCreaturesCannotCrew() {
        addWeatherlightReady(player1);
        Permanent creature = addCreatureReady(player1, new SerraAngel());
        creature.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }


    @Test
    @DisplayName("Deals combat damage and triggers look at top 5 with historic filter")
    void combatDamageTriggerWorks() {
        harness.setLife(player2, 20);
        Permanent weatherlight = addWeatherlightReady(player1);
        // Manually animate it as if crewed
        weatherlight.setAnimatedUntilEndOfTurn(true);
        weatherlight.setAnimatedPower(4);
        weatherlight.setAnimatedToughness(5);
        weatherlight.setAttacking(true);

        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // Weatherlight deals 4 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);

        // The combat damage trigger is on the stack — resolve it
        harness.passBothPriorities();

        // Trigger should have resolved — offering the legendary creature
        GameData gdAfter = harness.getGameData();
        assertThat(gdAfter.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gdAfter.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()).hasSize(1);
        assertThat(gdAfter.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards().getFirst().getName()).isEqualTo("Arvad the Cursed");
    }

    @Test
    @DisplayName("Combat damage trigger does not fire when blocked")
    void noCombatDamageTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent weatherlight = addWeatherlightReady(player1);
        weatherlight.setAnimatedUntilEndOfTurn(true);
        weatherlight.setAnimatedPower(4);
        weatherlight.setAnimatedToughness(5);
        weatherlight.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        setupTopCards(List.of(
                new ArvadTheCursed(),
                new GrizzlyBears(),
                new Shock(),
                new GrizzlyBears(),
                new GrizzlyBears()
        ));

        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        // No combat damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // No trigger fired
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canCrewWithSummoningSickCreature() {
        Permanent weatherlight = addWeatherlightReady(player1);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        angel.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(angel.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
    }

    @Test
    void canChooseLegendaryCreatureAndRandomlyBottomRest() {
        assertHistoricSelection(new ArvadTheCursed());
    }

    @Test
    void canChooseNonlegendaryArtifactAndRandomlyBottomRest() {
        assertHistoricSelection(new JoustingLance());
    }

    @Test
    void canChooseNonlegendarySagaAndRandomlyBottomRest() {
        assertHistoricSelection(new HistoryOfBenalia());
    }

    @Test
    void mayDeclineHistoricCardWithoutChoosingBottomOrder() {
        List<Card> looked = List.of(new ArvadTheCursed(), new SerraAngel(),
                new SerraAngel(), new SerraAngel(), new SerraAngel());
        SerraAngel untouched = new SerraAngel();
        List<Card> library = new ArrayList<>(looked);
        library.add(untouched);
        triggerFromCombat(library);

        chooseHistoricCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(looked);
    }

    @Test
    void noHistoricCardsAreRandomlyBottomedWithoutOrderingChoice() {
        List<Card> looked = List.of(new SerraAngel(), new SerraAngel(), new SerraAngel(),
                new SerraAngel(), new SerraAngel());
        JoustingLance sixth = new JoustingLance();
        List<Card> library = new ArrayList<>(looked);
        library.add(sixth);
        triggerFromCombat(library);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 6))
                .containsExactlyInAnyOrderElementsOf(looked);
    }

    @Test
    void looksAtAllCardsWhenLibraryHasFewerThanFive() {
        JoustingLance artifact = new JoustingLance();
        SerraAngel remaining = new SerraAngel();
        triggerFromCombat(List.of(artifact, remaining));

        assertThat(offeredHistoricCards()).containsExactly(artifact);
        chooseHistoricCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotCauseDrawOrChoice() {
        triggerFromCombat(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 16);
    }

    private void assertHistoricSelection(Card historic) {
        List<Card> rest = List.of(new SerraAngel(), new SerraAngel(),
                new SerraAngel(), new SerraAngel());
        SerraAngel untouched = new SerraAngel();
        List<Card> library = new ArrayList<>();
        library.add(historic);
        library.addAll(rest);
        library.add(untouched);
        triggerFromCombat(library);

        assertThat(offeredHistoricCards()).containsExactly(historic);
        chooseHistoricCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(historic);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrderElementsOf(rest);
    }

    private void triggerFromCombat(List<Card> library) {
        Permanent weatherlight = addWeatherlightReady(player1);
        addCreatureReady(player1, new SerraAngel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        weatherlight.setAttacking(true);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        resolveCombat();
        harness.passBothPriorities();
    }

    private List<Card> offeredHistoricCards() {
        PendingInteraction interaction = gd.interaction.activeInteraction();
        if (interaction instanceof PendingInteraction.LibrarySearch search) {
            return search.params().cards();
        }
        assertThat(interaction).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        PendingInteraction.LibraryRevealChoice choice = (PendingInteraction.LibraryRevealChoice) interaction;
        return choice.allCards().stream().filter(card -> choice.validCardIds().contains(card.getId())).toList();
    }

    private void chooseHistoricCard(int index) {
        List<Card> offered = offeredHistoricCards();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player1, index);
        } else {
            harness.handleMultipleCardsChosen(player1,
                    index < 0 ? List.of() : List.of(offered.get(index).getId()));
        }
    }

    private Permanent addWeatherlightReady(Player player) {
        return addCreatureReady(player, new Weatherlight());
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
