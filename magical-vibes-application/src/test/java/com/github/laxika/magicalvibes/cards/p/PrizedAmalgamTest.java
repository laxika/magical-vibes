package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrizedAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Returns tapped at the next end step when a creature enters from its graveyard")
    void returnsAfterCreatureEntersFromGraveyard() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam(), new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 1);
        resolveStack();

        harness.assertInGraveyard(player1, "Prized Amalgam");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent amalgam = findPermanent(player1, "Prized Amalgam");
        assertThat(amalgam).isNotNull();
        assertThat(amalgam.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns tapped when a creature is cast from its graveyard")
    void returnsAfterCreatureIsCastFromGraveyard() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam(), new WorldheartPhoenix()));
        addFiveColorsOfMana();

        harness.castFromGraveyard(player1, 1);
        resolveStack();

        harness.assertInGraveyard(player1, "Prized Amalgam");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent amalgam = findPermanent(player1, "Prized Amalgam");
        assertThat(amalgam).isNotNull();
        assertThat(amalgam.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when a creature is cast from hand")
    void doesNotTriggerForCreatureCastFromHand() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam()));
        harness.setHand(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveStack();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Prized Amalgam");
        harness.assertInGraveyard(player1, "Prized Amalgam");
    }

    private void addFiveColorsOfMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void resolveStack() {
        for (int i = 0; i < 12 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }
}
