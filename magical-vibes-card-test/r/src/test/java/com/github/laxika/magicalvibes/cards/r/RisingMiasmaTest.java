package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
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

@CardUsed({RisingMiasma.class, Forest.class, CoralhelmGuide.class, BroodhunterWurm.class,
        ScourFromExistence.class, SnappingGnarlid.class})
class RisingMiasmaTest extends BaseCardTest {

    @Test
    void givesAllCreaturesMinusTwoMinusTwo() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SnappingGnarlid());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());

        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    void alternateCastAwakensTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void alternateCastCannotTargetOpponentsLand() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, null, null, List.of(opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void normalCastCannotChooseAnAwakenTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void normalCastLeavesLandsUnanimatedAndReductionExpiresAtCleanup() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(1);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(3);
    }

    @Test
    void repeatedAwakenReducesExistingLandCreatureBeforeAddingCounters() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RisingMiasma(), new RisingMiasma()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(6);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void losingAwakenTargetPreventsCreatureReduction() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        harness.setHand(player1, List.of(new RisingMiasma()));
        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of(land.getId()));
        harness.castInstant(player2, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Rising Miasma");
    }
}
