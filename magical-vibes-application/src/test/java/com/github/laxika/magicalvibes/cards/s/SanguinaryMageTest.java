package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguinaryMage.class, FieryTemper.class, QuilledWolf.class, MagnifyingGlass.class})
class SanguinaryMageTest extends BaseCardTest {

    private Permanent addMage() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new SanguinaryMage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return mage;
    }

    @Test
    @DisplayName("Prowess gives Sanguinary Mage +1/+1 when its controller casts a noncreature spell")
    void noncreatureSpellPumps() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
    }

    @Test
    @DisplayName("Prowess does not trigger when its controller casts a creature spell")
    void creatureSpellDoesNotPump() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new QuilledWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess resolves before the triggering spell and triggers exactly once")
    void prowessResolvesBeforeSpell() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentsSpellDoesNotPump() {
        Permanent mage = addMage();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Prowess bonuses from multiple spells accumulate")
    void multipleSpellsAccumulate() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new FieryTemper(), new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting an artifact also triggers prowess")
    void artifactSpellPumps() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new MagnifyingGlass()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Magnifying Glass");
    }

    @Test
    @DisplayName("Resolving a creature spell leaves Sanguinary Mage unboosted")
    void resolvedCreatureDoesNotPump() {
        Permanent mage = addMage();
        harness.setHand(player1, List.of(new QuilledWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quilled Wolf");
        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller's noncreature spell triggers prowess during an opponent's turn")
    void controllersSpellOnOpponentTurnPumps() {
        Permanent mage = addMage();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new FieryTemper()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mage)).isEqualTo(4);
    }
}
