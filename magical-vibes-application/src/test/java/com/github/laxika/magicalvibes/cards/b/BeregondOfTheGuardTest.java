package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HumbleDefector;
import com.github.laxika.magicalvibes.cards.i.IncreasingDevotion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeregondOfTheGuard.class, Conspiracy.class, GrizzlyBears.class, HumbleDefector.class,
        ElvishMystic.class, IncreasingDevotion.class})
class BeregondOfTheGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Human entry gives all your creatures +1/+1 and vigilance")
    void ownHumanEntryBoostsAllOwnCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BeregondOfTheGuard(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent beregond = findPermanent(player1, "Beregond of the Guard");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, beregond)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beregond)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beregond, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A Human entering later triggers the ability")
    void laterHumanEntryTriggersAbility() {
        Permanent beregond = addCreatureReady(player1, new BeregondOfTheGuard());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new HumbleDefector(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent human = findPermanent(player1, "Humble Defector");
        assertThat(gqs.getEffectivePower(gd, beregond)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, beregond, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A non-Human creature entering does not trigger the ability")
    void nonHumanEntryDoesNotTriggerAbility() {
        Permanent beregond = addCreatureReady(player1, new BeregondOfTheGuard());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beregond)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, beregond, Keyword.VIGILANCE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The temporary boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BeregondOfTheGuard(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void opponentHumanEntryDoesNotTriggerAbility() {
        Permanent beregond = addCreatureReady(player1, new BeregondOfTheGuard());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new HumbleDefector(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, beregond)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beregond)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, beregond, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void abilityDoesNotBoostOpponentsCreatures() {
        Permanent opponentCreature = addCreatureReady(player2, new ElvishMystic());
        harness.castFromHand(player1, new BeregondOfTheGuard(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void creaturesEnteringAfterResolutionDoNotReceiveEarlierBoost() {
        Permanent beregond = addCreatureReady(player1, new BeregondOfTheGuard());
        harness.castFromHand(player1, new HumbleDefector(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new ElvishMystic(), "{G}");
        harness.passBothPriorities();

        Permanent mystic = findPermanent(player1, "Elvish Mystic");
        assertThat(gqs.getEffectivePower(gd, beregond)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, mystic)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, mystic)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, mystic, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void eachHumanTokenInASimultaneousEntryTriggersSeparately() {
        Permanent beregond = addCreatureReady(player1, new BeregondOfTheGuard());
        harness.castFromHand(player1, new IncreasingDevotion(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(5);
        for (int i = 0; i < 5; i++) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, beregond)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, beregond)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, beregond, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(5)
                .allSatisfy(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(6);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(6);
                    assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
                });
    }

    @Test
    void ownEntryTriggersEvenWhenBeregondIsNotHuman() {
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.castFromHand(player1, new BeregondOfTheGuard(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent beregond = findPermanent(player1, "Beregond of the Guard");
        assertThat(gqs.getEffectivePower(gd, beregond)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beregond)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beregond, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mystic)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mystic)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, mystic, Keyword.VIGILANCE)).isTrue();
    }
}
