package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VronosMaskedInquisitor.class, KarnScionOfUrza.class, JaceBeleren.class,
        GrizzlyBears.class, Millstone.class, Island.class})
class VronosMaskedInquisitorTest extends BaseCardTest {

    @Test
    void plusOnePhasesOutUpToTwoOtherPlaneswalkersAtNextEndStep() {
        Permanent vronos = addReadyVronos(player1, 4);
        Permanent karn = addReady(player1, new KarnScionOfUrza());
        Permanent jace = addReady(player1, new JaceBeleren());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(karn.getId(), jace.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(karn, jace);
        assertThat(vronos.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(karn, jace);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(karn, jace);
    }

    @Test
    void plusOneWithNoTargetsDoesNotPhaseOutVronos() {
        Permanent vronos = addReadyVronos(player1, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vronos);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).isNullOrEmpty();
    }

    @Test
    void minusTwoReturnsAtMostOneNonlandPermanentPerOpponent() {
        addReadyVronos(player1, 4);
        Permanent creature = addReady(player2, new GrizzlyBears());
        Permanent millstone = addReady(player2, new Millstone());
        Permanent land = addReady(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(creature.getId(), millstone.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(millstone, land).doesNotContain(creature);
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
    }

    @Test
    void minusSevenPermanentlyAnimatesTargetArtifact() {
        addReadyVronos(player1, 7);
        Permanent millstone = addReady(player1, new Millstone());

        harness.activateAbility(player1, 0, 2, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, millstone, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, millstone, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, millstone)).isTrue();
    }

    private Permanent addReadyVronos(Player player, int loyalty) {
        Permanent vronos = addReady(player, new VronosMaskedInquisitor());
        vronos.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return vronos;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
