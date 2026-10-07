package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HeartlessAct;
import com.github.laxika.magicalvibes.cards.h.HumbleNaturalist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnexpectedFangs.class, HumbleNaturalist.class, Forest.class, HeartlessAct.class})
class UnexpectedFangsTest extends BaseCardTest {

    @Test
    void putsPlusOnePlusOneAndLifelinkCountersOnTargetCreature() {
        Permanent target = addCreatureReady(player1, new HumbleNaturalist());
        harness.setHand(player1, List.of(new UnexpectedFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new UnexpectedFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void lifelinkBenefitsOpposingCreaturesController() {
        Permanent target = addCreatureReady(player2, new HumbleNaturalist());
        harness.setHand(player1, List.of(new UnexpectedFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    void multipleLifelinkCountersDoNotMultiplyLifeGain() {
        Permanent target = addCreatureReady(player1, new HumbleNaturalist());
        harness.setHand(player1, List.of(new UnexpectedFangs(), new UnexpectedFangs()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(2);
        declareAttackers(List.of(0));
        resolveCombat();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    @Test
    void countersRemainAfterTurnEnds() {
        Permanent target = addCreatureReady(player1, new HumbleNaturalist());
        harness.setHand(player1, List.of(new UnexpectedFangs()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void removedTargetReceivesNeitherCounter() {
        Permanent target = addCreatureReady(player1, new HumbleNaturalist());
        harness.setHand(player1, List.of(new UnexpectedFangs()));
        harness.setHand(player2, List.of(new HeartlessAct()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Humble Naturalist");
        harness.assertInGraveyard(player1, "Humble Naturalist");
        harness.assertInGraveyard(player1, "Unexpected Fangs");
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isZero();
    }
}
