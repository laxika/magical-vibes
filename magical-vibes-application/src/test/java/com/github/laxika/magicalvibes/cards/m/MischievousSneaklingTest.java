package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChampionOfTheClachan;
import com.github.laxika.magicalvibes.cards.e.EclipsedElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MischievousSneakling.class, ChampionOfTheClachan.class, EclipsedElf.class})
class MischievousSneaklingTest extends BaseCardTest {

    @Test
    void canCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MischievousSneakling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.passPriority(player2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void changelingReceivesKithkinBoost() {
        Permanent sneakling = harness.addToBattlefieldAndReturn(player1, new MischievousSneakling());
        int originalPower = gqs.getEffectivePower(gd, sneakling);
        int originalToughness = gqs.getEffectiveToughness(gd, sneakling);
        harness.addToBattlefield(player1, new ChampionOfTheClachan());

        assertThat(gqs.getEffectivePower(gd, sneakling)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, sneakling)).isEqualTo(originalToughness + 1);
    }

    @Test
    void changelingCanBeRevealedAsElfFromLibrary() {
        MischievousSneakling sneakling = new MischievousSneakling();
        harness.setLibrary(player1, List.of(sneakling));
        harness.setHand(player1, List.of(new EclipsedElf()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(sneakling.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sneakling);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canCastInResponseToCreatureSpellWithBlackHybridPayment() {
        harness.setHand(player1, List.of(new MischievousSneakling()));
        harness.setHand(player2, List.of(new MischievousSneakling()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);

        harness.passPriority(player1);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Mischievous Sneakling");
        harness.assertOnBattlefield(player1, "Mischievous Sneakling");
    }
}
