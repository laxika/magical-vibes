package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SepharaSkysBlade.class, SuntailHawk.class, GrizzlyBears.class})
class SepharaSkysBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {W} and four untapped creatures with flying")
    void castsForAlternateCost() {
        List<UUID> flyerIds = List.of(
                addFlyer().getId(),
                addFlyer().getId(),
                addFlyer().getId(),
                addFlyer().getId());
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreatureWithAlternateCost(player1, 0, flyerIds);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sephara, Sky's Blade");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> flyerIds.contains(permanent.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Alternate cost requires four qualifying creatures")
    void alternateCostRequiresFourFlyers() {
        List<UUID> flyerIds = List.of(addFlyer().getId(), addFlyer().getId(), addFlyer().getId());
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, flyerIds))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonflying creature cannot pay Sephara's alternate cost")
    void alternateCostRequiresFlyingCreatures() {
        List<UUID> permanentIds = List.of(
                addFlyer().getId(),
                addFlyer().getId(),
                addFlyer().getId(),
                harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId());
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, permanentIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("Other creatures you control with flying have indestructible")
    void grantsIndestructibleToOtherFlyingCreaturesYouControl() {
        Permanent sephara = harness.addToBattlefieldAndReturn(player1, new SepharaSkysBlade());
        Permanent ownFlyer = addFlyer();
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        assertThat(gqs.hasKeyword(gd, ownFlyer, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownGroundCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingFlyer, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, sephara, Keyword.INDESTRUCTIBLE)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(sephara);

        assertThat(gqs.hasKeyword(gd, ownFlyer, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Alternate cost cannot count the same creature more than once")
    void alternateCostRequiresDistinctCreatures() {
        Permanent selectedFlyer = addFlyer();
        addFlyer();
        addFlyer();
        addFlyer();
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID selectedId = selectedFlyer.getId();

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0,
                List.of(selectedId, selectedId, selectedId, selectedId)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(selectedFlyer.isTapped()).isFalse();
        harness.assertInHand(player1, "Sephara, Sky's Blade");
    }

    @Test
    @DisplayName("Already tapped creatures cannot pay the alternate cost")
    void alternateCostRejectsTappedCreature() {
        Permanent tappedFlyer = addFlyer();
        tappedFlyer.tap();
        List<UUID> flyerIds = List.of(tappedFlyer.getId(), addFlyer().getId(),
                addFlyer().getId(), addFlyer().getId());
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, flyerIds))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opposing creatures cannot pay the alternate cost")
    void alternateCostRejectsOpposingCreature() {
        Permanent opposingFlyer = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        List<UUID> flyerIds = List.of(addFlyer().getId(), addFlyer().getId(),
                addFlyer().getId(), opposingFlyer.getId());
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, flyerIds))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Four flying creatures do not replace the white mana payment")
    void alternateCostRequiresWhiteMana() {
        List<UUID> flyerIds = List.of(addFlyer().getId(), addFlyer().getId(),
                addFlyer().getId(), addFlyer().getId());
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, flyerIds))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sephara can be cast for her normal mana cost without other creatures")
    void castsForNormalManaCost() {
        harness.setHand(player1, List.of(new SepharaSkysBlade()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sephara, Sky's Blade");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addFlyer() {
        return harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
    }
}
