package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.cards.h.HedronCrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyScourer.class, HedronCrawler.class, CanopyGorger.class})
class SkyScourerTest extends BaseCardTest {

    private Permanent addSkyScourer() {
        Permanent scourer = harness.addToBattlefieldAndReturn(player1, new SkyScourer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return scourer;
    }

    @Test
    @DisplayName("Casting a colorless spell gives Sky Scourer +1/+0 until end of turn")
    void colorlessSpellPumps() {
        Permanent scourer = addSkyScourer();

        harness.setHand(player1, List.of(new HedronCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a colored spell does not trigger Sky Scourer")
    void coloredSpellDoesNotPump() {
        Permanent scourer = addSkyScourer();

        harness.setHand(player1, List.of(new CanopyGorger()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Sky Scourer boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent scourer = addSkyScourer();

        harness.setHand(player1, List.of(new HedronCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(2);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }

    @Test
    @DisplayName("A devoid creature triggers Sky Scourer despite its black mana cost")
    void devoidSpellPumpsBeforeSpellResolves() {
        Permanent scourer = addSkyScourer();
        harness.setHand(player1, List.of(new SkyScourer()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        Permanent newlyEntered = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.getEffectivePower(gd, newlyEntered)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple colorless spells give cumulative boosts until cleanup")
    void multipleColorlessSpellsStack() {
        Permanent scourer = addSkyScourer();
        harness.setHand(player1, List.of(new HedronCrawler(), new HedronCrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's colorless spell does not trigger Sky Scourer")
    void opponentColorlessSpellDoesNotPump() {
        Permanent scourer = addSkyScourer();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SkyScourer()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, scourer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scourer)).isEqualTo(2);
    }
}
