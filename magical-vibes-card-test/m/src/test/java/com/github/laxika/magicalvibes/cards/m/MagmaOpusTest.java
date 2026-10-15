package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaOpus.class, GrizzlyBears.class, HillGiant.class})
class MagmaOpusTest extends BaseCardTest {

    @Test
    void dealsDividedDamageTapsPermanentsCreatesElementalAndDrawsTwoCards() {
        Permanent damageTarget1 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent damageTarget2 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent tapTarget1 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent tapTarget2 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears drawn1 = new GrizzlyBears();
        GrizzlyBears drawn2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn1, drawn2));
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null,
                Map.of(damageTarget1.getId(), 2, damageTarget2.getId(), 2),
                List.of(tapTarget1.getId(), tapTarget2.getId()), List.of());
        harness.passBothPriorities();

        assertThat(damageTarget1.getMarkedDamage()).isEqualTo(2);
        assertThat(damageTarget2.getMarkedDamage()).isEqualTo(2);
        assertThat(tapTarget1.isTapped()).isTrue();
        assertThat(tapTarget2.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Elemental")).singleElement().satisfies(elemental -> {
            assertThat(elemental.getEffectivePower()).isEqualTo(4);
            assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        });
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn1, drawn2);
    }

    @Test
    void allowsDamageTargetToAlsoBeATapTarget() {
        Permanent sharedTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherTapTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null,
                Map.of(sharedTarget.getId(), 4),
                List.of(sharedTarget.getId(), otherTapTarget.getId()), List.of());

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.targetsForGroup(0)).containsExactly(sharedTarget.getId());
        assertThat(entry.targetsForGroup(1)).containsExactly(sharedTarget.getId(), otherTapTarget.getId());
    }

    @Test
    void requiresTwoTapTargets() {
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(damageTarget.getId(), 4), List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void handAbilityCreatesTreasureAndDiscardsMagmaOpus() {
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
        harness.assertInGraveyard(player1, "Magma Opus");
    }
    @Test
    void resolvesRemainingEffectsWhenOneDamageTargetIsGone() {
        Permanent removedTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent remainingTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears drawn1 = new GrizzlyBears();
        GrizzlyBears drawn2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn1, drawn2));
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null,
                Map.of(removedTarget.getId(), 2, remainingTarget.getId(), 2),
                List.of(removedTarget.getId(), remainingTarget.getId()), List.of());
        gd.playerBattlefields.get(player2.getId()).remove(removedTarget);
        harness.passBothPriorities();

        assertThat(remainingTarget.getMarkedDamage()).isEqualTo(2);
        assertThat(remainingTarget.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn1, drawn2);
    }

    @Test
    void doesNothingWhenAllTargetsAreGone() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears drawn1 = new GrizzlyBears();
        GrizzlyBears drawn2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn1, drawn2));
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 0, null,
                Map.of(first.getId(), 4), List.of(first.getId(), second.getId()), List.of());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn1, drawn2);
        harness.assertInGraveyard(player1, "Magma Opus");
    }

    @Test
    void dealsDamageToPlayerAndCanTapOwnAlreadyTappedPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        first.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLife(player2, 20);

        gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 4), List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void rejectsChoosingSamePermanentTwiceForTapping() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null,
                Map.of(player2.getId(), 4), List.of(target.getId(), target.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void handAbilityCanUseMixedHybridPaymentAndDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Magma Opus");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void handAbilityCanUseOnlyRedMana() {
        harness.setHand(player1, List.of(new MagmaOpus()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInGraveyard(player1, "Magma Opus");
    }
}
