package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaplingNurseryTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1} less to cast for each Forest you control")
    void costsLessForEachForest() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SaplingNursery()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Landfall creates a 3/4 green Treefolk token with reach")
    void landfallCreatesTreefolk() {
        harness.addToBattlefield(player1, new SaplingNursery());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent treefolk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treefolk"))
                .findFirst()
                .orElseThrow();
        assertThat(treefolk.getEffectivePower()).isEqualTo(3);
        assertThat(treefolk.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Exiling Sapling Nursery grants indestructible to Treefolk and Forests until end of turn")
    void activationGrantsIndestructibleToTreefolkAndForests() {
        Permanent nursery = harness.addToBattlefieldAndReturn(player1, new SaplingNursery());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        Permanent treefolk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Treefolk"))
                .findFirst()
                .orElseThrow();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(nursery);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, mountain, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, treefolk, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A Forest controlled by an opponent does not reduce the cost")
    void opponentForestDoesNotReduceCost() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SaplingNursery()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
