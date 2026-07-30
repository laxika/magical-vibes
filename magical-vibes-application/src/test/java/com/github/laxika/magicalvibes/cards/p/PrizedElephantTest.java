package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrizedElephantTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/3 with no Forest")
    void noBoostWithoutForest() {
        harness.addToBattlefield(player1, new PrizedElephant());

        Permanent elephant = findPermanent(player1, "Prized Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-Forest land does not grant the boost")
    void noBoostWithNonForestLand() {
        harness.addToBattlefield(player1, new PrizedElephant());
        harness.addToBattlefield(player1, new Plains());

        Permanent elephant = findPermanent(player1, "Prized Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 while controlling a Forest")
    void boostWithForest() {
        harness.addToBattlefield(player1, new PrizedElephant());
        harness.addToBattlefield(player1, new Forest());

        Permanent elephant = findPermanent(player1, "Prized Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's Forest does not grant the boost")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player1, new PrizedElephant());
        harness.addToBattlefield(player2, new Forest());

        Permanent elephant = findPermanent(player1, "Prized Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Loses the boost when the Forest leaves")
    void losesBoostWhenForestLeaves() {
        harness.addToBattlefield(player1, new PrizedElephant());
        harness.addToBattlefield(player1, new Forest());

        Permanent elephant = findPermanent(player1, "Prized Elephant");
        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Forest"));

        assertThat(gqs.getEffectivePower(gd, elephant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elephant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating the ability grants trample")
    void abilityGrantsTrample() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new PrizedElephant());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent elephant = harness.addToBattlefieldAndReturn(player1, new PrizedElephant());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elephant, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without green mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new PrizedElephant());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
