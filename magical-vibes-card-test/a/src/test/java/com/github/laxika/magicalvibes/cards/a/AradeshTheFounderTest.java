package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.b.BenalishFaithbonder;
import com.github.laxika.magicalvibes.cards.d.DelneyStreetwiseLookout;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.i.IsshinTwoHeavensAsOne;
import com.github.laxika.magicalvibes.cards.l.LinebreakerBaloth;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AradeshTheFounder.class, CentaurCourser.class, GrizzlyBears.class,
        GiantGrowth.class, Unsummon.class, BenalishFaithbonder.class, DelneyStreetwiseLookout.class, IsshinTwoHeavensAsOne.class, LinebreakerBaloth.class})
class AradeshTheFounderTest extends BaseCardTest {

    @BeforeEach
    void askForTheTriggerOrder() {
        harness.setAutoOrderTriggers(player1, false);
    }

    @Test
    void enlistingGrantsDoubleStrikeAndDrawsAtPowerThreshold() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aradesh)));
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(supporter.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        stackAradeshsAbilityBelowTheEnlistBoost();
        resolveAllTriggers();

        assertThat(supporter.isTapped()).isTrue();
        assertThat(aradesh.getPowerModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void enlistingBelowPowerThresholdStillGrantsDoubleStrikeWithoutDrawing() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aradesh)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        stackAradeshsAbilityBelowTheEnlistBoost();
        resolveAllTriggers();

        assertThat(aradesh.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void doesNotTriggerWhenTheAttackerDoesNotEnlist() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void decliningEnlistDoesNotGrantDoubleStrikeOrDraw() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(supporter.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    void enlistUsesSupportersPowerWhenItsTriggerResolves() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        stackAradeshsAbilityBelowTheEnlistBoost();
        harness.castAndResolveInstant(player1, 0, supporter.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, aradesh)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsUsingLastKnownPowerWhenAttackerLeavesBeforeRewardResolves() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        stackAradeshsAbilityBelowTheEnlistBoost();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, aradesh)).isEqualTo(4);
        harness.castAndResolveInstant(player1, 0, aradesh.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aradesh);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void opponentsAradeshDoesNotRewardYourEnlistingCreature() {
        harness.setHand(player1, List.of());
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        addCreatureReady(player2, new AradeshTheFounder());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        stackAradeshsAbilityBelowTheEnlistBoost();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
    }

    @Test
    void rewardsAnotherEnlistingAttackerWhileAradeshStaysBack() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent attacker = addCreatureReady(player1, new BenalishFaithbonder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        stackAradeshsAbilityBelowTheEnlistBoost();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Delney makes the ability trigger an additional time: it is Aradesh's ability, so Aradesh's power counts")
    void delneyDoublesTheAbilityForAnAttackerWithGreaterPower() {
        harness.setAutoOrderTriggers(player1, true);
        addCreatureReady(player1, new AradeshTheFounder());
        addCreatureReady(player1, new DelneyStreetwiseLookout());
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(baloth)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, baloth, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    @DisplayName("Isshin makes the ability trigger an additional time: a creature attacking triggers it")
    void isshinMakesTheAbilityTriggerAnAdditionalTime() {
        harness.setAutoOrderTriggers(player1, true);
        addCreatureReady(player1, new AradeshTheFounder());
        addCreatureReady(player1, new IsshinTwoHeavensAsOne());
        Permanent baloth = addCreatureReady(player1, new LinebreakerBaloth());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(baloth)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, baloth, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    @DisplayName("Its controller may order the ability to resolve before the enlist boost; it then sees the lower power")
    void abilityOrderedBeforeTheEnlistBoostSeesTheUnboostedPower() {
        Permanent aradesh = addCreatureReady(player1, new AradeshTheFounder());
        Permanent supporter = addCreatureReady(player1, new CentaurCourser());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aradesh)));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.chooseTriggerToStackNext(player1, "enlist");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, aradesh, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(aradesh.getPowerModifier()).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    /**
     * CR 603.3b: the controller orders the abilities that triggered together. Puts Aradesh's ability on the stack
     * first, so the enlist boost resolves before it.
     */
    private void stackAradeshsAbilityBelowTheEnlistBoost() {
        while (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice order
                && order.context() instanceof ChoiceContext.TriggerOrder) {
            harness.handleListChoice(player1, order.options().stream()
                    .filter(option -> !option.contains("enlist"))
                    .findFirst().orElse(order.options().getFirst()));
        }
    }
}
