package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BaskingCapybara;
import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HiddenNursery;
import com.github.laxika.magicalvibes.cards.r.RootMaze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spelunking.class, Forest.class, BaskingCapybara.class, HiddenNursery.class, RootMaze.class, BloodMoon.class})
class SpelunkingTest extends BaseCardTest {

    @Test
    void drawsAndGainsLifeWhenPuttingACaveOntoTheBattlefield() {
        HiddenNursery cave = new HiddenNursery();
        harness.setHand(player1, List.of(new Spelunking(), cave));
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Untapped");

        harness.assertLife(player1, 24);
        harness.assertOnBattlefield(player1, "Hidden Nursery");
        harness.assertInHand(player1, "Basking Capybara");
    }

    @Test
    void decliningTheLandDropDoesNotGainLife() {
        harness.setHand(player1, List.of(new Spelunking(), new Forest()));
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Basking Capybara");
    }

    @Test
    void puttingANonCaveLandDoesNotGainLife() {
        harness.setHand(player1, List.of(new Spelunking(), new Forest()));
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
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
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Untapped");

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    void canPutTheCaveDrawnByTheEnterAbilityOntoTheBattlefield() {
        harness.setLibrary(player1, List.of(new HiddenNursery()));
        harness.castFromHand(player1, new Spelunking(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Untapped");

        harness.assertOnBattlefield(player1, "Hidden Nursery");
        harness.assertNotInHand(player1, "Hidden Nursery");
        harness.assertLife(player1, 24);
        assertThat(findPermanent(player1, "Hidden Nursery").isTapped()).isFalse();
    }

    @Test
    void choosingTappedCaveEntryStillGainsLife() {
        harness.setHand(player1, List.of(new Spelunking(), new HiddenNursery()));
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleListChoice(player1, "Tapped");

        harness.assertLife(player1, 24);
        assertThat(findPermanent(player1, "Hidden Nursery").isTapped()).isTrue();
    }

    @Test
    void opponentsCaveStillEntersTapped() {
        harness.addToBattlefield(player1, new Spelunking());
        harness.setHand(player2, List.of(new HiddenNursery()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player2, 0);

        assertThat(findPermanent(player2, "Hidden Nursery").isTapped()).isTrue();
        harness.assertLife(player2, 20);
    }

    @Test
    void drawingANonlandWithNoLandInHandDoesNotGainLife() {
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
        harness.castFromHand(player1, new Spelunking(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Basking Capybara");
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Basking Capybara");
    }


    @Test
    void caveEnteringAsAMountainUnderBloodMoonDoesNotGainLife() {
        harness.addToBattlefield(player2, new BloodMoon());
        harness.setHand(player1, List.of(new Spelunking(), new HiddenNursery()));
        harness.setLibrary(player1, List.of(new BaskingCapybara()));
        addManaForSpelunking();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Hidden Nursery");
        harness.assertLife(player1, 20);
    }

    private void addManaForSpelunking() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
