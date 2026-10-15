package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.MysterioMasterOfIllusion;
import com.github.laxika.magicalvibes.cards.s.SpiderManWebSlinger;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VultureSchemingScavenger.class, MysterioMasterOfIllusion.class, SpiderManWebSlinger.class})
class VultureSchemingScavengerTest extends BaseCardTest {

    @Test
    void otherVillainsYouControlGainFlyingWhenVultureAttacks() {
        addCreatureReady(player1, new VultureSchemingScavenger());
        Permanent villain = addCreatureReady(player1, new MysterioMasterOfIllusion());
        Permanent nonVillain = addCreatureReady(player1, new SpiderManWebSlinger());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonVillain, Keyword.FLYING)).isFalse();
    }

    @Test
    void opponentsVillainsDoNotGainFlying() {
        addCreatureReady(player1, new VultureSchemingScavenger());
        Permanent opponentVillain = addCreatureReady(player2, new MysterioMasterOfIllusion());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, opponentVillain, Keyword.FLYING)).isFalse();
    }

    @Test
    void grantedFlyingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new VultureSchemingScavenger());
        Permanent villain = addCreatureReady(player1, new MysterioMasterOfIllusion());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isFalse();
    }

    @Test
    void villainEnteringBeforeTriggerResolvesGainsFlying() {
        addCreatureReady(player1, new VultureSchemingScavenger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent villain = addCreatureReady(player1, new MysterioMasterOfIllusion());
        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isFalse();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isTrue();
    }

    @Test
    void villainEnteringAfterTriggerResolvesDoesNotGainFlying() {
        addCreatureReady(player1, new VultureSchemingScavenger());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent villain = addCreatureReady(player1, new MysterioMasterOfIllusion());

        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isFalse();
    }

    @Test
    void anotherVillainAttackingDoesNotTriggerVulture() {
        addCreatureReady(player1, new VultureSchemingScavenger());
        Permanent villain = addCreatureReady(player1, new MysterioMasterOfIllusion());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, villain, Keyword.FLYING)).isFalse();
    }
}
