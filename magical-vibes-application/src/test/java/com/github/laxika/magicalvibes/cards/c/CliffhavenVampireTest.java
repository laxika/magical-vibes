package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CliffhavenVampire.class, AngelOfMercy.class, GrizzlyBears.class, SoulWarden.class})
class CliffhavenVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent loses 1 life when controller gains life")
    void opponentLosesLifeOnControllerLifeGain() {
        harness.addToBattlefield(player1, new CliffhavenVampire());

        int startingLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Loses only 1 life regardless of the amount of life gained")
    void losesOneLifeRegardlessOfAmountGained() {
        harness.addToBattlefield(player1, new CliffhavenVampire());
        harness.addToBattlefield(player1, new SoulWarden());

        int startingLife = gd.getLife(player2.getId());
        int controllerLife = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Does not trigger when the opponent gains life")
    void doesNotTriggerOnOpponentLifeGain() {
        harness.addToBattlefield(player1, new CliffhavenVampire());

        int startingLife = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife + 3);
    }

    @Test
    @DisplayName("Each Cliffhaven Vampire triggers separately on one life gain event")
    void eachCliffhavenVampireTriggersSeparately() {
        harness.addToBattlefield(player1, new CliffhavenVampire());
        harness.addToBattlefield(player1, new CliffhavenVampire());

        int startingLife = gd.getLife(player2.getId());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
    }
}
