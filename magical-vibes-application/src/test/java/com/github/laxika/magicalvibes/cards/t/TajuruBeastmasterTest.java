package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TajuruBeastmaster.class, GrizzlyBears.class, SeaGateLoremaster.class,
        SnappingGnarlid.class, Conspiracy.class})
class TajuruBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures +1/+1 until end of turn")
    void ownAllyEntryBoostsYourCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent beastmaster = harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Another Ally entry gives your creatures +1/+1 until end of turn")
    void anotherAllyEntryBoostsYourCreatures() {
        Permanent beastmaster = addCreatureReady(player1, new TajuruBeastmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        Permanent ally = harness.enterBattlefieldAndReturn(player1, new SeaGateLoremaster());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ally)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ally)).isEqualTo(4);
    }

    @Test
    @DisplayName("A non-Ally or opponent Ally entry does not trigger it")
    void nonAllyAndOpponentAllyEntriesDoNotTrigger() {
        Permanent beastmaster = addCreatureReady(player1, new TajuruBeastmaster());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player2, new SeaGateLoremaster());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void creaturesEnteringBeforeResolutionAreBoostedButLaterCreaturesAreNot() {
        Permanent opponent = addCreatureReady(player2, new SnappingGnarlid());
        Permanent beastmaster = harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());

        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new SnappingGnarlid());
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    void twoBeastmastersEachTriggerWhenAnotherBeastmasterEnters() {
        Permanent first = addCreatureReady(player1, new TajuruBeastmaster());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(7);
    }

    @Test
    void rallyResolvesAfterItsSourceLeavesTheBattlefield() {
        Permanent creature = addCreatureReady(player1, new SnappingGnarlid());
        Permanent beastmaster = harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, beastmaster));

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void ownEntryTriggersEvenWhenConspiracyReplacesTheAllyType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        Permanent beastmaster = harness.enterBattlefieldAndReturn(player1, new TajuruBeastmaster());
        assertThat(gqs.hasEffectiveSubtype(gd, beastmaster, CardSubtype.ALLY)).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, beastmaster)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, beastmaster)).isEqualTo(6);
    }
}
