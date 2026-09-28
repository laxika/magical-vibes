package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KardurDoomscourge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SereneSleuth.class, GrizzlyBears.class, KardurDoomscourge.class})
class SereneSleuthTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Clue token")
    void investigatesOnEnter() {
        harness.setHand(player1, List.of(new SereneSleuth()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Investigates for each goaded creature, then removes goad")
    void investigatesAndRemovesGoadAtBeginningOfCombat() {
        Permanent sleuth = addCreatureReady(player2, new SereneSleuth());
        Permanent firstCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player2, new GrizzlyBears());

        castKardur();
        assertThat(gqs.isGoaded(gd, sleuth)).isTrue();
        assertThat(gqs.isGoaded(gd, firstCreature)).isTrue();
        assertThat(gqs.isGoaded(gd, secondCreature)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Clue")).hasSize(3);
        assertThat(gqs.isGoaded(gd, sleuth)).isFalse();
        assertThat(gqs.isGoaded(gd, firstCreature)).isFalse();
        assertThat(gqs.isGoaded(gd, secondCreature)).isFalse();
    }

    private void castKardur() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new KardurDoomscourge()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
