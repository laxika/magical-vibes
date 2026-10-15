package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiveteersRequisitioner.class, GrizzlyBears.class, NantukoHusk.class})
class RiveteersRequisitionerTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast creates a Treasure on death without drawing")
    void normalCastCreatesTreasureWithoutBlitzDraw() {
        harness.addToBattlefield(player1, new NantukoHusk());
        harness.setHand(player1, List.of(new RiveteersRequisitioner()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent requisitioner = findPermanent(player1, "Riveteers Requisitioner");
        assertThat(gqs.hasKeyword(gd, requisitioner, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, requisitioner.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Riveteers Requisitioner");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, creates a Treasure, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndCreatesTreasure() {
        harness.setHand(player1, List.of(new RiveteersRequisitioner()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent requisitioner = findPermanent(player1, "Riveteers Requisitioner");
        assertThat(gqs.hasKeyword(gd, requisitioner, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(requisitioner);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Riveteers Requisitioner");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Blitz grants haste as the creature spell resolves without an entry trigger")
    void blitzHasHasteImmediatelyOnEntry() {
        harness.setHand(player1, List.of(new RiveteersRequisitioner()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent requisitioner = findPermanent(player1, "Riveteers Requisitioner");
        assertThat(gqs.hasKeyword(gd, requisitioner, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blitz draws and creates a Treasure when sacrificed before the end step")
    void blitzDeathBeforeEndStepDrawsAndCreatesTreasure() {
        harness.addToBattlefield(player1, new NantukoHusk());
        harness.setHand(player1, List.of(new RiveteersRequisitioner()));
        harness.setLibrary(player1, List.of(new RiveteersRequisitioner()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        Permanent requisitioner = findPermanent(player1, "Riveteers Requisitioner");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, requisitioner.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Riveteers Requisitioner");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("A normally cast Requisitioner survives the end step")
    void normalCastIsNotSacrificedAtEndStep() {
        harness.setHand(player1, List.of(new RiveteersRequisitioner()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Riveteers Requisitioner");
        harness.assertNotInGraveyard(player1, "Riveteers Requisitioner");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
