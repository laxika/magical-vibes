package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CruciasTitanOfTheWaves.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class CruciasTitanOfTheWavesTest extends BaseCardTest {

    @Test
    void ambitiousCreatesTreasureAndSeeksGreaterManaValueCard() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears equal = new GrizzlyBears();
        HillGiant greater = new HillGiant();
        Shock lesser = new Shock();
        harness.addToBattlefield(player1, new CruciasTitanOfTheWaves());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(equal, greater, lesser));

        resolveEndStepTrigger(true, "Ambitious");

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(greater);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equal, lesser);
    }

    @Test
    void expedientCreatesTreasureAndSeeksLesserManaValueCard() {
        GrizzlyBears discarded = new GrizzlyBears();
        GrizzlyBears equal = new GrizzlyBears();
        HillGiant greater = new HillGiant();
        Shock lesser = new Shock();
        harness.addToBattlefield(player1, new CruciasTitanOfTheWaves());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(equal, greater, lesser));

        resolveEndStepTrigger(true, "Expedient");

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).contains(lesser);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equal, greater);
    }

    @Test
    void decliningDoesNothing() {
        GrizzlyBears cardInHand = new GrizzlyBears();
        HillGiant libraryCard = new HillGiant();
        harness.addToBattlefield(player1, new CruciasTitanOfTheWaves());
        harness.setHand(player1, List.of(cardInHand));
        harness.setLibrary(player1, List.of(libraryCard));

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void treasureAndModeChoiceHappenDuringTheSameResolutionAsTheDiscard() {
        GrizzlyBears discarded = new GrizzlyBears();
        HillGiant greater = new HillGiant();
        harness.addToBattlefield(player1, new CruciasTitanOfTheWaves());
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(greater));

        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleListChoice(player1, "Ambitious");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(greater);
    }

    private void resolveEndStepTrigger(boolean acceptDiscard, String mode) {
        advanceToEndStep(player1);
        harness.handleMayAbilityChosen(player1, acceptDiscard);
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, mode);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
