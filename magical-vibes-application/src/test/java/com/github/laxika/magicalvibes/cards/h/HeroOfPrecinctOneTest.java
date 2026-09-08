package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GloryscaleViashino;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeroOfPrecinctOneTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell creates a 1/1 white Human token")
    void multicoloredSpellCreatesHuman() {
        addCreatureReady(player1, new HeroOfPrecinctOne());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new GloryscaleViashino()));
        addGloryscaleViashinoMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Human");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a monocolored spell does not create a Human token")
    void monocoloredSpellDoesNotCreateHuman() {
        addCreatureReady(player1, new HeroOfPrecinctOne());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human")).isZero();
    }

    @Test
    @DisplayName("An opponent casting a multicolored spell does not create a Human token")
    void opponentSpellDoesNotCreateHuman() {
        addCreatureReady(player1, new HeroOfPrecinctOne());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GloryscaleViashino()));
        addGloryscaleViashinoMana(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Human")).isZero();
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addGloryscaleViashinoMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }
}
