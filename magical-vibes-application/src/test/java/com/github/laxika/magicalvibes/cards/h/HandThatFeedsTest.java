package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HandThatFeeds.class, GrizzlyBears.class, Forest.class, Shock.class, Pacifism.class})
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
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The delirium condition is checked again as the attack trigger resolves")
    void rechecksDeliriumAtResolution() {
        Permanent hand = addCreatureReady(player1, new HandThatFeeds());
        setDelirium();

        declareAttackers(player1, List.of(0));
        gd.playerGraveyards.get(player1.getId()).removeLast();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isFalse();
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, hand)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, hand, Keyword.MENACE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }
}
