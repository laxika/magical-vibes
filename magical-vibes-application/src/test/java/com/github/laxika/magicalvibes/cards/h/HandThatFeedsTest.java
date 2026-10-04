package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AttackInTheBox;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JumpScare;
import com.github.laxika.magicalvibes.cards.s.ShardmagesRescue;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HandThatFeeds.class, AttackInTheBox.class, Forest.class, JumpScare.class, ShardmagesRescue.class})
class HandThatFeedsTest extends BaseCardTest {

    @Test
    @DisplayName("Delirium gives Hand That Feeds +2/+0 and menace when it attacks")
    void deliriumBoostsAndGrantsMenaceOnAttack() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        setDelirium();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Without delirium, attacking does not grant the bonus")
    void doesNotTriggerWithoutDelirium() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        harness.setGraveyard(player1, List.of(new HandThatFeeds(), new Forest(), new JumpScare()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Losing delirium after attacking does not stop the attack bonus")
    void losingDeliriumAfterAttackDoesNotStopBonus() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        setDelirium();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The attack bonus wears off at end of turn")
    void bonusWearsOffAtEndOfTurn() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        setDelirium();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("One card can contribute two types toward delirium")
    void multipleTypesOnOneCardCountTowardDelirium() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        harness.setGraveyard(player1, List.of(
                new AttackInTheBox(), new JumpScare(), new ShardmagesRescue()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable delirium")
    void opponentsGraveyardDoesNotEnableDelirium() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(
                new HandThatFeeds(), new Forest(), new JumpScare(), new ShardmagesRescue()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Gaining delirium after attacking does not create an attack trigger")
    void gainingDeliriumAfterAttackDoesNotGrantBonus() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        harness.setGraveyard(player1, List.of(new HandThatFeeds(), new Forest(), new JumpScare()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).isEmpty();
        setDelirium();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new HandThatFeeds(), new Forest(), new JumpScare(), new ShardmagesRescue()));
    }
}
