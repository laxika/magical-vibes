package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.e.EchoOfDeathsWail;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JungleHollow;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.cards.t.TributeToHorobi;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarchOfOtherworldlyLight.class, GrizzlyBears.class, HillGiant.class, SavannahLions.class,
        AncestralKatana.class, JungleHollow.class, TributeToHorobi.class, EchoOfDeathsWail.class})
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

    @Test
    @DisplayName("Exiles a noncreature artifact controlled by the caster")
    void exilesOwnNoncreatureArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AncestralKatana());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForX(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Ancestral Katana");
    }

    @Test
    @DisplayName("Exiles a noncreature enchantment")
    void exilesNoncreatureEnchantment() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TributeToHorobi());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantForX(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Tribute to Horobi");
    }

    @Test
    @DisplayName("A land without an eligible permanent type cannot be targeted even at X zero")
    void rejectsOrdinaryLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JungleHollow());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantForX(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Multiple white cards may be exiled beyond the needed reduction, before resolution")
    void exilesMultipleCardsWithSpellInMiddleOfHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        harness.setHand(player1, List.of(new AncestralKatana(), new MarchOfOtherworldlyLight(),
                new AncestralKatana()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantForXWithDiscards(player1, 1, 2, List.of(target.getId()), List.of(0, 2));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Ancestral Katana", "Ancestral Katana");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(3);
    }

    @Test
    @DisplayName("The white mana requirement cannot be reduced by exiling cards")
    void exileReductionCannotPayWhiteManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight(), new AncestralKatana(),
                new AncestralKatana()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(1);
    }

    @Test
    @DisplayName("The spell itself cannot be exiled to pay its additional cost")
    void cannotExileSpellItself() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight(), new AncestralKatana()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 2, List.of(target.getId()), List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("One white card cannot be counted twice toward the cost reduction")
    void rejectsDuplicateExileSelections() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight(), new AncestralKatana()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantForXWithDiscards(
                player1, 0, 4, List.of(target.getId()), List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Two exiled white cards allow X four while paying only white mana")
    void twoExiledCardsReduceCostByFourWithoutReducingX() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight(), new AncestralKatana(),
                new AncestralKatana()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstantForXWithDiscards(player1, 0, 4, List.of(target.getId()), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Ancestral Katana", "Ancestral Katana", "Hill Giant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cards exiled as a cost stay exiled when the target leaves before resolution")
    void paidExileCostRemainsWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight(), new AncestralKatana()));
        harness.setHand(player2, List.of(new MarchOfOtherworldlyLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(target.getId()), List.of(1));
        harness.castInstantForX(player2, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactly("Ancestral Katana", "Ancestral Katana");
        harness.assertInGraveyard(player1, "March of Otherworldly Light");
        harness.assertInGraveyard(player2, "March of Otherworldly Light");
    }
}
