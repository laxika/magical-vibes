package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaryFarmer.class})
class WaryFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils 1 at your end step after another creature entered under your control")
    void surveilsAfterAnotherCreatureEnteredUnderYourControl() {
        WaryFarmer farmer = new WaryFarmer();
        harness.addToBattlefield(player1, farmer);
        Card enteredCreature = new WaryFarmer();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(enteredCreature);
        Card topCard = new WaryFarmer();
        harness.setLibrary(player1, List.of(topCard));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Does not trigger when only Wary Farmer entered under your control")
    void doesNotTriggerForItsOwnEntry() {
        WaryFarmer farmer = new WaryFarmer();
        harness.addToBattlefield(player1, farmer);
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(farmer);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a creature that entered under an opponent's control")
    void doesNotTriggerForOpponentCreatureEntry() {
        harness.addToBattlefield(player1, new WaryFarmer());
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player2.getId(), ignored -> new ArrayList<>())
                .add(new WaryFarmer());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May keep the surveilled card on top of the library")
    void mayKeepSurveilledCard() {
        harness.addToBattlefield(player1, new WaryFarmer());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                new ArrayList<>(List.of(new WaryFarmer())));
        Card topCard = new WaryFarmer();
        Card nextCard = new WaryFarmer();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger at an opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new WaryFarmer());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                new ArrayList<>(List.of(new WaryFarmer())));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveils an empty library without requiring a choice")
    void surveilsEmptyLibrary() {
        harness.addToBattlefield(player1, new WaryFarmer());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                new ArrayList<>(List.of(new WaryFarmer())));
        harness.setLibrary(player1, List.of());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A resolved Wary Farmer does not count its own actual entry")
    void doesNotTriggerForItsOwnResolvedEntry() {
        harness.setHand(player1, List.of(new WaryFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveils only once even when several other creatures entered")
    void surveilsOnceForMultipleEntries() {
        harness.addToBattlefield(player1, new WaryFarmer());
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(),
                new ArrayList<>(List.of(new WaryFarmer(), new WaryFarmer())));
        Card topCard = new WaryFarmer();
        Card nextCard = new WaryFarmer();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A different creature's actual entry enables the end-step surveil")
    void surveilsAfterAnotherCreatureResolves() {
        harness.addToBattlefield(player1, new WaryFarmer());
        harness.setHand(player1, List.of(new WaryFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Card topCard = new WaryFarmer();
        harness.setLibrary(player1, List.of(topCard));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }
    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
