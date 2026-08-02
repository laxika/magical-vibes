package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NavSquadCommandosTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion gives Nav Squad Commandos +1/+1 and untaps it")
    void battalionBoostsAndUntapsSource() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(commandos.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(commandos.getPowerModifier()).isEqualTo(1);
        assertThat(commandos.getToughnessModifier()).isEqualTo(1);
        assertThat(commandos.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attacking creatures")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(commandos.getPowerModifier()).isZero();
        assertThat(commandos.getToughnessModifier()).isZero();
        assertThat(commandos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures do not count toward battalion")
    void nonAttackingCreaturesDoNotCount() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(commandos.getPowerModifier()).isZero();
        assertThat(commandos.getToughnessModifier()).isZero();
    }
}
