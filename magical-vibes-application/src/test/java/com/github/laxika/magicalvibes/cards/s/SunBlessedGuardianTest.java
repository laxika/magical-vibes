package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FurnaceBlessedConqueror;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunBlessedGuardian.class, FurnaceBlessedConqueror.class, GrizzlyBears.class})
class SunBlessedGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("The transform ability can pay its Phyrexian mana with life")
    void transformsByPayingPhyrexianManaWithLife() {
        Permanent guardian = addGuardian();
        int startingLife = gd.getLife(player1.getId());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(guardian.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("The back face creates a tapped and attacking copy carrying the source's counters")
    void attackCreatesTappedAttackingCopyWithSourceCounters() {
        Permanent guardian = addTransformedGuardian();
        guardian.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        preventAutoPass();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(5);
        assertThat(gd.getDelayedActions(DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost.class))
                .contains(new DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost(
                        token.getId(), player1.getId(), Integer.MAX_VALUE));
    }

    @Test
    @DisplayName("The attack copy is sacrificed at the next end step")
    void attackCopyIsSacrificedAtNextEndStep() {
        addTransformedGuardian();
        preventAutoPass();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    private Permanent addGuardian() {
        harness.addToBattlefield(player1, new SunBlessedGuardian());
        Permanent guardian = gd.playerBattlefields.get(player1.getId()).getFirst();
        guardian.setSummoningSick(false);
        return guardian;
    }

    private Permanent addTransformedGuardian() {
        Permanent guardian = addGuardian();
        guardian.setCard(guardian.getCard().getBackFaceCard());
        guardian.setTransformed(true);
        return guardian;
    }

    private void preventAutoPass() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }
}
