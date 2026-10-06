package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanctumPlowbeast.class, Plains.class, Island.class, Forest.class, GrizzlyBears.class})
class SanctumPlowbeastTest extends BaseCardTest {

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingDiscardsAndOffersPlains() {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum Plowbeast");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Plains"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Choosing a Plains from the search puts it into hand")
    void choosingPlainsPutsItIntoHand() {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Islandcycling discards the card and offers only Island cards")
    void islandcyclingDiscardsAndOffersIslands() {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        setupLibrary();

        harness.ensurePriority(player1);
        harness.getGameService().activateHandAbility(gd, player1, 0, 1, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum Plowbeast");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.getName().equals("Island"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Choosing an Island puts exactly that card into hand")
    void choosingIslandPutsItIntoHand() {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        activateCycling(1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Sanctum Plowbeast");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Cycling pays the discard cost before the search resolves")
    void discardsBeforeResolution(int abilityIndex) {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        activateCycling(abilityIndex);

        harness.assertInGraveyard(player1, "Sanctum Plowbeast");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Cycling may fail to find even when a matching land exists")
    void canFailToFind(int abilityIndex) {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        setupLibrary();

        activateCycling(abilityIndex);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Cycling resolves without drawing when the library is empty")
    void emptyLibraryDoesNotDrawOrLose(int abilityIndex) {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of());

        activateCycling(abilityIndex);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanctum Plowbeast");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("One mana cannot pay either cycling cost")
    void insufficientManaDoesNotDiscard(int abilityIndex) {
        harness.setHand(player1, List.of(new SanctumPlowbeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> activateCycling(abilityIndex))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Sanctum Plowbeast");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Defender prevents Sanctum Plowbeast from attacking")
    void cannotAttack() {
        addCreatureReady(player1, new SanctumPlowbeast());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private void activateCycling(int abilityIndex) {
        harness.ensurePriority(player1);
        gs.activateHandAbility(gd, player1, 0, abilityIndex, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Island(),
                new Forest(), new GrizzlyBears()));
    }
}
