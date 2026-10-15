package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MakindiPatrol;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AppaTheVigilant.class, GrizzlyBears.class, MakindiPatrol.class, Conspiracy.class})
class AppaTheVigilantTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry boosts and grants flying and vigilance to your creatures")
    void ownAllyEntryBoostsAndGrantsKeywords() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new AppaTheVigilant(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent appa = findPermanent(player1, "Appa, the Vigilant");
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(appa.getPowerModifier()).isEqualTo(1);
        assertThat(appa.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Another Ally entering triggers the boost")
    void anotherAllyEntryTriggers() {
        Permanent appa = harness.addToBattlefieldAndReturn(player1, new AppaTheVigilant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new MakindiPatrol(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(appa.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entering does not trigger it")
    void nonAllyEntryDoesNotTrigger() {
        Permanent appa = harness.addToBattlefieldAndReturn(player1, new AppaTheVigilant());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(appa.getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost and granted keywords wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new AppaTheVigilant(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(ownCreature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Appa's own entry still triggers when Conspiracy replaces its Ally type")
    void ownEntryTriggersWithoutAllyType() {
        Permanent conspiracy = harness.addToBattlefieldAndReturn(player1, new Conspiracy());
        conspiracy.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new AppaTheVigilant(), "{5}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's Ally entering does not trigger Appa")
    void opponentsAllyEntryDoesNotTrigger() {
        Permanent appa = harness.addToBattlefieldAndReturn(player1, new AppaTheVigilant());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MakindiPatrol(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(appa.getPowerModifier()).isZero();
        assertThat(appa.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the earlier boost")
    void laterCreatureDoesNotReceiveResolvedBoost() {
        harness.castFromHand(player1, new AppaTheVigilant(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent laterCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(laterCreature.getPowerModifier()).isZero();
        assertThat(laterCreature.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.VIGILANCE)).isFalse();
    }
}
