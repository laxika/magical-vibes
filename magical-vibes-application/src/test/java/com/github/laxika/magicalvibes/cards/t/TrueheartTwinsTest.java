package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HoodedBrawler;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrueheartTwins.class, HoodedBrawler.class})
class TrueheartTwinsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addReadyTwins(player1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting gives every creature you control +1/+0 until end of turn")
    void exertBoostsYourCreatures() {
        Permanent twins = addReadyTwins(player1);
        Permanent brawler = addCreatureReady(player1, new HoodedBrawler());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, twins)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brawler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exerting keeps the creature tapped through its next untap step")
    void exertSkipsNextUntap() {
        Permanent twins = addReadyTwins(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(twins.isTapped()).isTrue();
        assertThat(twins.getSkipUntapCount()).isGreaterThan(0);

        harness.performUntapStep(player2);
        assertThat(twins.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(twins.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(twins.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining exert leaves creatures at base stats")
    void decliningExertDoesNothing() {
        Permanent twins = addReadyTwins(player1);
        Permanent brawler = addCreatureReady(player1, new HoodedBrawler());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
        assertThat(twins.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent twins = addReadyTwins(player1);
        Permanent brawler = addCreatureReady(player1, new HoodedBrawler());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);

        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exerting another creature also gives creatures you control +1/+0")
    void exertingAnotherCreatureTriggers() {
        Permanent twins = addReadyTwins(player1);
        Permanent brawler = addCreatureReady(player1, new HoodedBrawler());

        declareAttackers(List.of(1));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(6);
        assertThat(twins.getSkipUntapCount()).isZero();
    }


    @Test
    @DisplayName("The boost excludes opposing creatures and creatures entering after resolution")
    void boostAffectsOnlyYourCreaturesAtResolution() {
        Permanent twins = addReadyTwins(player1);
        Permanent opponent = addCreatureReady(player2, new HoodedBrawler());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        Permanent lateArrival = addCreatureReady(player1, new HoodedBrawler());

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateArrival)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each copy triggers independently when another creature is exerted")
    void multipleTwinsEachBoostTheTeam() {
        Permanent first = addReadyTwins(player1);
        Permanent second = addReadyTwins(player1);
        Permanent brawler = addCreatureReady(player1, new HoodedBrawler());

        declareAttackers(List.of(2));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(7);
    }

    @Test
    @DisplayName("An opponent exerting a creature does not trigger your Twins")
    void opponentExertDoesNotTriggerYourTwins() {
        Permanent twins = addReadyTwins(player1);
        Permanent brawler = addCreatureReady(player2, new HoodedBrawler());

        declareAttackers(player2, List.of(0));
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, twins)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, brawler)).isEqualTo(5);
    }

    private Permanent addReadyTwins(Player player) {
        return addCreatureReady(player, new TrueheartTwins());
    }
}
