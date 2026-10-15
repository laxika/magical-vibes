package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.ThrabenValiant;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelsTomb.class, ThrabenValiant.class, MarchOfTheMachines.class})
class AngelsTombTest extends BaseCardTest {

    @Test
    void tombEnteringAsACreatureTriggersItsOwnAbility() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent tomb = harness.enterBattlefieldAndReturn(player1, new AngelsTomb());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, tomb)).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertAnimated(tomb);
    }

    @Test
    void acceptingCreatureEntryAnimatesOnlyTheTomb() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new ThrabenValiant());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gqs.isCreature(gd, tomb)).isFalse();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertAnimated(tomb);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    void decliningCreatureEntryLeavesTombAnArtifact() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player1, new ThrabenValiant());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, tomb)).isFalse();
        assertThat(gqs.isArtifact(gd, tomb)).isTrue();
    }

    @Test
    void animationExpiresAtEndOfTurn() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player1, new ThrabenValiant());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertAnimated(tomb);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tomb)).isFalse();
        assertThat(gqs.isArtifact(gd, tomb)).isTrue();
        assertThat(gqs.hasKeyword(gd, tomb, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, tomb, CardSubtype.ANGEL)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, tomb)).isEmpty();
    }

    @Test
    void opponentCreatureDoesNotTriggerTomb() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player2, new ThrabenValiant());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, tomb)).isFalse();
    }

    @Test
    void noncreatureEntryDoesNotTriggerTomb() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player1, new AngelsTomb());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.isCreature(gd, tomb)).isFalse();
    }

    @Test
    void laterCreatureEntryStillTriggersAnAnimatedTomb() {
        Permanent tomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player1, new ThrabenValiant());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.enterBattlefieldAndReturn(player1, new ThrabenValiant());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertAnimated(tomb);
    }

    @Test
    void leavingAndReturningDoesNotAnimateTheNewTomb() {
        Permanent oldTomb = harness.addToBattlefieldAndReturn(player1, new AngelsTomb());
        harness.enterBattlefieldAndReturn(player1, new ThrabenValiant());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        gd.playerBattlefields.get(player1.getId()).remove(oldTomb);
        Permanent newTomb = harness.enterBattlefieldAndReturn(player1, oldTomb.getCard());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, newTomb)).isFalse();
        assertThat(gqs.isArtifact(gd, newTomb)).isTrue();
    }

    private void assertAnimated(Permanent tomb) {
        assertThat(gqs.isCreature(gd, tomb)).isTrue();
        assertThat(gqs.isArtifact(gd, tomb)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tomb)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tomb)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, tomb)).containsExactly(CardColor.WHITE);
        assertThat(gqs.hasEffectiveSubtype(gd, tomb, CardSubtype.ANGEL)).isTrue();
        assertThat(gqs.hasKeyword(gd, tomb, Keyword.FLYING)).isTrue();
    }
}
