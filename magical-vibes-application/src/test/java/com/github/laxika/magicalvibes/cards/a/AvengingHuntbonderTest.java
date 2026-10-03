package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvengingHuntbonder.class, GrizzlyBears.class})
class AvengingHuntbonderTest extends BaseCardTest {

    @Test
    void putsDoubleStrikeCounterOnAnotherAttackingCreature() {
        Permanent huntbonder = addReadyCreature(new AvengingHuntbonder());
        Permanent attacker = addReadyCreature(new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(huntbonder.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
    }

    @Test
    void cannotTargetItself() {
        Permanent huntbonder = addReadyCreature(new AvengingHuntbonder());
        addReadyCreature(new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, huntbonder.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonAttackingCreature() {
        addReadyCreature(new AvengingHuntbonder());
        addReadyCreature(new GrizzlyBears());
        Permanent nonAttacker = addReadyCreature(new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void hasNoTargetChoiceWhenAttackingAlone() {
        addReadyCreature(new AvengingHuntbonder());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotPutCounterOnCreatureThatStopsAttackingBeforeResolution() {
        addReadyCreature(new AvengingHuntbonder());
        Permanent attacker = addReadyCreature(new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.DOUBLE_STRIKE)).isZero();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void resolvesAfterHuntbonderLeavesBattlefield() {
        Permanent huntbonder = addReadyCreature(new AvengingHuntbonder());
        Permanent attacker = addReadyCreature(new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(huntbonder);
        gd.playerGraveyards.get(player1.getId()).add(huntbonder.getCard());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void counterMakesUnblockedAttackerDealDamageTwice() {
        addReadyCreature(new AvengingHuntbonder());
        Permanent attacker = addReadyCreature(new GrizzlyBears());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(attacker.getCounterCount(CounterType.DOUBLE_STRIKE)).isEqualTo(1);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
