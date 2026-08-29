package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaliciousAdviceTest extends BaseCardTest {

    @Test
    @DisplayName("Taps exactly X artifacts, creatures, and lands and makes its controller lose X life")
    void tapsMixedPermanentTypesAndLosesXLife() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ConjurersBauble());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 3, List.of(artifact.getId(), creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("X=0 requires no targets and causes no life loss")
    void xZeroDoesNothing() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantForX(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a permanent that is not an artifact, creature, or land")
    void cannotTargetOtherPermanent() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraFlux());
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 1, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Must choose exactly X targets")
    void requiresExactlyXTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaliciousAdvice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
