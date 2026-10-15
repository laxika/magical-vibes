package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.m.ManorSkeleton;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThrabenSentry.class, CruelEdict.class, GrizzlyBears.class, DeadWeight.class, ManorSkeleton.class})
class ThrabenSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when another creature you control dies and you choose yes")
    void transformsWhenAllyCreatureDiesAccept() {
        harness.addToBattlefield(player1, new ThrabenSentry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent sentry = findPermanent(player1, "Thraben Sentry");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        // Sacrifice the Grizzly Bears, keeping Thraben Sentry
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        // ON_ALLY_CREATURE_DIES trigger goes on stack → resolve → MayEffect prompts
        harness.passBothPriorities();

        // Accept the may transform
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentry.isTransformed()).isTrue();
        assertThat(sentry.getCard().getName()).isEqualTo("Thraben Militia");
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not transform when you choose no")
    void doesNotTransformWhenDeclined() {
        harness.addToBattlefield(player1, new ThrabenSentry());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent sentry = findPermanent(player1, "Thraben Sentry");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sentry.isTransformed()).isFalse();
        assertThat(sentry.getCard().getName()).isEqualTo("Thraben Sentry");
    }

    @Test
    @DisplayName("Does not trigger when opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new ThrabenSentry());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        // No trigger on the stack — ON_ALLY_CREATURE_DIES only fires for controller's creatures
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger when Thraben Sentry itself dies (only 'another creature')")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new ThrabenSentry());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        GameData gd = harness.getGameData();
        // No trigger — Sentry is no longer on the battlefield
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Thraben Sentry");
    }

    @Test
    @DisplayName("Overlapping death triggers cannot transform Militia back to Sentry")
    void overlappingDeathTriggersTransformOnlyOnce() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new ThrabenSentry());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        first.setMarkedDamage(1);
        second.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(sentry.isTransformed()).isTrue();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sentry.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Thraben Militia");
    }

    @Test
    @DisplayName("Militia does not trigger when another allied creature dies")
    void militiaDoesNotTriggerOnLaterDeath() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new ThrabenSentry());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        first.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(sentry.isTransformed()).isTrue();

        Permanent second = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        second.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(sentry.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("A continuous penalty applies to the transformed face")
    void continuousPenaltyAppliesToTransformedFace() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new ThrabenSentry());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        victim.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, sentry.getId());
        harness.passBothPriorities();

        assertThat(sentry.isTransformed()).isTrue();
        assertThat(findPermanent(player1, "Dead Weight").getAttachedTo()).isEqualTo(sentry.getId());
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sentry attacks without tapping")
    void sentryAttacksWithoutTapping() {
        Permanent sentry = addCreatureReady(player1, new ThrabenSentry());
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(sentry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Militia taps to attack and tramples over a blocker")
    void militiaTapsAndTramples() {
        Permanent sentry = addCreatureReady(player1, new ThrabenSentry());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new ManorSkeleton());
        victim.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(sentry.isTransformed()).isTrue();

        Permanent blocker = addCreatureReady(player2, new ManorSkeleton());
        harness.setLife(player2, 20);
        declareAttackersAndPrepareBlockers(List.of(0));
        assertThat(sentry.isTapped()).isTrue();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 1, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Manor Skeleton");
        assertThat(sentry.getMarkedDamage()).isEqualTo(1);
    }
}
