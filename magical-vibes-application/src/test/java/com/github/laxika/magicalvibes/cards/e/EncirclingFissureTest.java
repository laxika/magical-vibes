package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EncirclingFissure.class, Island.class, GiantMantis.class})
class EncirclingFissureTest extends BaseCardTest {

    @Test
    void preventsCombatDamageFromCreaturesControlledByTargetOpponent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GiantMantis());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantMantis());
        castNormallyAt(player2.getId());

        assertThat(gqs.isPreventedFromDealingDamage(gd, opponentCreature, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, opponentCreature, false)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, ownCreature, true)).isFalse();
    }

    @Test
    void alternateCastAwakensTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new EncirclingFissure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveCardTypes(gd, land)).contains(CardType.LAND);
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        harness.setHand(player1, List.of(new EncirclingFissure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    private void castNormallyAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new EncirclingFissure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    void normalCastDoesNotAnimateOrAddCountersToLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        castNormallyAt(player2.getId());

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void normalCastCannotChooseAnAwakenTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new EncirclingFissure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(player2.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new EncirclingFissure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void preventionAlsoAppliesToCreaturesEnteringAfterResolution() {
        castNormallyAt(player2.getId());

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantMantis());

        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, false)).isFalse();
    }

    @Test
    void preventionExpiresButAwakenPersistsAfterTurnEnds() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantMantis());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        castAwakenAt(land);
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void awakeningAnAlreadyAwakenedLandAddsCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        castAwakenAt(land);
        harness.passBothPriorities();
        castAwakenAt(land);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
    }

    @Test
    void stillPreventsCombatDamageWhenAwakenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantMantis());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        castAwakenAt(land);
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isTrue();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotAwakenAnOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> castAwakenAt(land))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAwakenAt(Permanent land) {
        harness.setHand(player1, List.of(new EncirclingFissure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), land.getId()));
    }
}
