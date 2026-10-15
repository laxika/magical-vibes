package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BroodhunterWurm;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarthenArms.class, Forest.class, BroodhunterWurm.class})
class EarthenArmsTest extends BaseCardTest {

    @Test
    void putsTwoCountersOnTargetPermanentNormally() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void alternateCastAddsCountersAndAwakensTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId(), land.getId()));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isLand(gameData, land)).isTrue();
        assertThat(gqs.isCreature(gameData, land)).isTrue();
        assertThat(gqs.getEffectivePower(gameData, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gameData, land)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gameData, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gameData, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void alternateCastRequiresAwakenLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void alternateCastCannotTargetOpponentsLandForAwaken() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId(), opponentLand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land you control");
    }

    @Test
    void normalCastCanPutCountersOnOpponentsNoncreatureLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, land.getId());

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void awakenCanUseSameLandForBothTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(land.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void awakenStillResolvesWhenOriginalTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BroodhunterWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId(), land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
    }

    @Test
    void originalEffectStillResolvesWhenAwakenLandLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodhunterWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EarthenArms()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId(), land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }
}
