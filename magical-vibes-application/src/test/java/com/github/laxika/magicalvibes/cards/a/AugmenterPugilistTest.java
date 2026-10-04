package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BayouGroff;
import com.github.laxika.magicalvibes.cards.b.BuryInBooks;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TanazirQuandrix;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AugmenterPugilist.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        BayouGroff.class, BuryInBooks.class, TanazirQuandrix.class})
class AugmenterPugilistTest extends BaseCardTest {

    @Test
    void getsPlusFivePlusFiveWithEightLands() {
        Permanent augmenter = addCreatureReady(player1, new AugmenterPugilist());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        int powerBefore = gqs.getEffectivePower(gd, augmenter);
        int toughnessBefore = gqs.getEffectiveToughness(gd, augmenter);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, augmenter)).isEqualTo(powerBefore + 5);
        assertThat(gqs.getEffectiveToughness(gd, augmenter)).isEqualTo(toughnessBefore + 5);
    }

    @Test
    void echoingEquationCopiesOnlyOtherControlledCreaturesWithoutLegendary() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        TestCards.mutableCard(target).setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent other = addCreatureReady(player1, new HillGiant());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new AugmenterPugilist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(other.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(other.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(opponentCreature.getCard().getName()).isEqualTo("Hill Giant");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(other.getCard().getName()).isEqualTo("Hill Giant");
    }

    @Test
    void echoingEquationCannotTargetAnOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new AugmenterPugilist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(
                player1, 0, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void frontFaceCanBeCastForItsOwnManaCost() {
        harness.castFromHand(player1, new AugmenterPugilist(), "{1}{G}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Augmenter Pugilist")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bonusDisappearsBelowEightLandsAndIgnoresOpponentsLands() {
        Permanent augmenter = addCreatureReady(player1, new AugmenterPugilist());
        int basePower = gqs.getEffectivePower(gd, augmenter);
        int baseToughness = gqs.getEffectiveToughness(gd, augmenter);
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player2, new Forest());
        }
        assertThat(gqs.getEffectivePower(gd, augmenter)).isEqualTo(basePower + 5);
        assertThat(gqs.getEffectiveToughness(gd, augmenter)).isEqualTo(baseToughness + 5);

        gd.playerBattlefields.get(player1.getId()).removeLast();

        assertThat(gqs.getEffectivePower(gd, augmenter)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, augmenter)).isEqualTo(baseToughness);
    }

    @Test
    void copiesRealLegendWithoutCopyingCountersOrTriggeringEnterAbilities() {
        Permanent target = addCreatureReady(player1, new TanazirQuandrix());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent other = addCreatureReady(player1, new BayouGroff());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        other.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new AugmenterPugilist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(other.getCard().getName()).isEqualTo("Tanazir Quandrix");
        assertThat(other.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(other.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);
        assertThat(other.isTapped()).isTrue();
        assertThat(land.getCard().getName()).isEqualTo("Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        Permanent laterCreature = addCreatureReady(player1, new BayouGroff());
        assertThat(laterCreature.getCard().getName()).isEqualTo("Bayou Groff");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(other.getCard().getName()).isEqualTo("Tanazir Quandrix");
        harness.passBothPriorities();
        assertThat(other.getCard().getName()).isEqualTo("Bayou Groff");
        assertThat(other.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    void copiesLandDependentAbilityWithoutCopyingItsCurrentBonusTwice() {
        Permanent target = addCreatureReady(player1, new AugmenterPugilist());
        Permanent other = addCreatureReady(player1, new BayouGroff());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new AugmenterPugilist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(8);
        gd.playerBattlefields.get(player1.getId()).removeLast();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    void doesNotCopyWhenTheOnlyTargetLeavesBeforeResolution() {
        Permanent target = addCreatureReady(player1, new AugmenterPugilist());
        Permanent other = addCreatureReady(player1, new BayouGroff());
        harness.setHand(player1, List.of(new AugmenterPugilist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castModalSorcery(player1, 0, 1, List.of(target.getId()));

        harness.setHand(player2, List.of(new BuryInBooks()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(other.getCard().getName()).isEqualTo("Bayou Groff");
        assertThat(gd.stack).isEmpty();
    }
}
