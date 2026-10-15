package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EldraziSkyspawner;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpawnbedProtector.class, EldraziSkyspawner.class, GrizzlyBears.class})
class SpawnbedProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, returns an Eldrazi creature and creates two Scions")
    void returnsEldraziAndCreatesScions() {
        EldraziSkyspawner eldrazi = new EldraziSkyspawner();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eldrazi, bears));
        castSpawnbedProtector();

        advanceToEndStep();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eldrazi.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eldrazi.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(eldrazi);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eldrazi);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    @DisplayName("Eldrazi Scions sacrifice for colorless mana")
    void scionsSacrificeForColorlessMana() {
        castSpawnbedProtector();
        advanceToEndStep();

        harness.passBothPriorities();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("Choosing no graveyard target still creates two Scions")
    void createsScionsWhenChoosingNoTarget() {
        SpawnbedProtector eldrazi = new SpawnbedProtector();
        harness.setGraveyard(player1, List.of(eldrazi));
        castSpawnbedProtector();
        advanceToEndStep();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eldrazi);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eldrazi);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    @DisplayName("An illegal sole graveyard target prevents Scion creation")
    void doesNotCreateScionsWhenTargetLeavesGraveyard() {
        SpawnbedProtector eldrazi = new SpawnbedProtector();
        harness.setGraveyard(player1, List.of(eldrazi));
        castSpawnbedProtector();
        advanceToEndStep();

        harness.handleMultipleCardsChosen(player1, List.of(eldrazi.getId()));
        harness.setGraveyard(player1, List.of());
        gd.addToExile(player1.getId(), eldrazi);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(eldrazi);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("The return and Scion creation resolve together")
    void returnsCardAndCreatesScionsInOneResolution() {
        SpawnbedProtector eldrazi = new SpawnbedProtector();
        harness.setGraveyard(player1, List.of(eldrazi));
        castSpawnbedProtector();
        advanceToEndStep();

        harness.handleMultipleCardsChosen(player1, List.of(eldrazi.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(eldrazi);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        castSpawnbedProtector();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("Only the controller's graveyard supplies targets, and only one card returns")
    void returnsOnlyOneCardFromControllersGraveyard() {
        SpawnbedProtector first = new SpawnbedProtector();
        SpawnbedProtector second = new SpawnbedProtector();
        SpawnbedProtector opponentsCard = new SpawnbedProtector();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opponentsCard));
        castSpawnbedProtector();
        advanceToEndStep();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second, opponentsCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(2);
    }

    private void castSpawnbedProtector() {
        harness.setHand(player1, List.of(new SpawnbedProtector()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
