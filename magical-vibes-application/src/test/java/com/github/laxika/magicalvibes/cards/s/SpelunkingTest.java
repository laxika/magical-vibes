package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RootMaze;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spelunking.class, Forest.class, GrizzlyBears.class, RootMaze.class})
class SpelunkingTest extends BaseCardTest {

    @Test
    void drawsAndGainsLifeWhenPuttingACaveOntoTheBattlefield() {
        Forest cave = new Forest();
        cave.setSubtypes(List.of(CardSubtype.CAVE));
        harness.setHand(player1, List.of(new Spelunking(), cave));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 24);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void decliningTheLandDropDoesNotGainLife() {
        harness.setHand(player1, List.of(new Spelunking(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void puttingANonCaveLandDoesNotGainLife() {
        harness.setHand(player1, List.of(new Spelunking(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void controlledLandsEnterUntapped() {
        harness.addToBattlefield(player1, new RootMaze());
        harness.setHand(player1, List.of(new Spelunking(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    private void addManaForSpelunking() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
