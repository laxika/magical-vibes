package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalametBrawler.class, ArmoredKincaller.class})
class MalametBrawlerTest extends BaseCardTest {

    @Test
    void grantsTrampleToAnotherAttackingCreature() {
        Permanent brawler = addCreatureReady(player1, new MalametBrawler());
        Permanent otherAttacker = addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent brawler = addCreatureReady(player1, new MalametBrawler());
        addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, brawler.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new MalametBrawler());
        addCreatureReady(player1, new ArmoredKincaller());
        Permanent nonAttacker = addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedTrampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MalametBrawler());
        Permanent attacker = addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void doesNotTriggerWhenOnlyAnotherCreatureAttacks() {
        Permanent brawler = addCreatureReady(player1, new MalametBrawler());
        Permanent attacker = addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canTargetItselfWhenAttackingAlone() {
        Permanent brawler = addCreatureReady(player1, new MalametBrawler());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, brawler.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brawler, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotGrantTrampleIfTargetStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new MalametBrawler());
        Permanent attacker = addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void triggerStillResolvesAfterBrawlerLeavesBattlefield() {
        Permanent brawler = addCreatureReady(player1, new MalametBrawler());
        Permanent attacker = addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, brawler));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Malamet Brawler");
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }
}