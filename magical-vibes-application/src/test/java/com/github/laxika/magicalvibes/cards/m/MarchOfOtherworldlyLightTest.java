package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfOtherworldlyLight.class, GrizzlyBears.class, HillGiant.class, SavannahLions.class})
class MarchOfOtherworldlyLightTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target artifact, creature, or enchantment within X")
    void exilesTargetWithinX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(target.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Exiling a white card from hand reduces the generic cost by two")
    void exilingWhiteCardReducesGenericCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight(), new SavannahLions()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(target.getId()), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Savannah Lions", "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The optional hand exile cost only accepts white cards")
    void handExileCostRequiresWhiteCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight(), new HillGiant()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    @DisplayName("A target with mana value above X is illegal")
    void rejectsTargetAboveX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }
}
