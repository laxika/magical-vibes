package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShiftyDoppelgangerTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen creature enters with haste and returns Shifty Doppelganger after being sacrificed")
    void chosenCreatureIsSacrificedAndSourceReturns() {
        harness.addToBattlefield(player1, new ShiftyDoppelganger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        activateAndChooseCreature();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));

        advanceToEndStep();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanent(player1, "Shifty Doppelganger")).isNotNull();
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));
    }

    @Test
    @DisplayName("Declining the creature choice leaves Shifty Doppelganger exiled")
    void decliningLeavesSourceExiled() {
        harness.addToBattlefield(player1, new ShiftyDoppelganger());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Shifty Doppelganger")).isEmpty();
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Shifty Doppelganger"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    private void activateAndChooseCreature() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
