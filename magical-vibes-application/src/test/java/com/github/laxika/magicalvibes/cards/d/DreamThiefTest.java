package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DreamThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when another blue spell was cast this turn")
    void drawsAfterAnotherBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.setHand(player1, List.of(new FugitiveWizard(), new DreamThief()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0); // blue spell
        harness.passBothPriorities();
        harness.castCreature(player1, 0); // Dream Thief
        harness.passBothPriorities(); // Dream Thief resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves

        // ETB drew a card — the one remaining library Forest is now in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Dream Thief");
    }

    @Test
    @DisplayName("Does not draw when it is the only spell cast this turn")
    void noDrawWhenNoOtherSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.setHand(player1, List.of(new DreamThief()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dream Thief");
    }

    @Test
    @DisplayName("Does not draw when the only other spell cast was not blue")
    void noDrawAfterNonBlueSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.setHand(player1, List.of(new GrizzlyBears(), new DreamThief()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0); // green spell
        harness.passBothPriorities();
        harness.castCreature(player1, 0); // Dream Thief
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Dream Thief");
    }
}
