package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CowardKiller.class, GiantSpider.class, GrizzlyBears.class, Forest.class})
class CowardKillerTest extends BaseCardTest {

    private static final int COWARD = 0;
    private static final int KILLER = 1;

    @Test
    @DisplayName("Coward makes the target unable to block, adds Coward, and time travels")
    void cowardModeAppliesAllEffects() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        target.setCounterCount(CounterType.TIME, 1);
        harness.setHand(player1, List.of(new CowardKiller()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalSorcery(player1, 0, COWARD, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).contains(CardSubtype.COWARD);
        assertThat(target.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Killer damages the target and all creatures sharing a creature type")
    void killerDamagesCreatureTypeMatchesAcrossBattlefields() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent matching = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent different = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CowardKiller()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castModalSorcery(player1, 0, KILLER, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(matching.getMarkedDamage()).isEqualTo(3);
        assertThat(different.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Both halves can target only creatures")
    void bothModesRejectNonCreatureTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CowardKiller()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, COWARD, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, KILLER, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
