package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BorosEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a bonus when attacking with fewer than two other creatures")
    void noBonusWithFewerThanTwoOtherAttackers() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isZero();
        assertThat(elite.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Gets +2/+2 when attacking with two other creatures")
    void getsBonusWithTwoOtherAttackers() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isEqualTo(2);
        assertThat(elite.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not count toward battalion")
    void nonAttackingCreaturesDoNotCount() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isZero();
        assertThat(elite.getToughnessModifier()).isZero();
    }
}
