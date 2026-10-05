package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LargeBear;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MomentOfGlory.class, LargeBear.class, Forest.class})
class MomentOfGloryTest extends BaseCardTest {

    @Test
    void normalCastPutsCounterOnlyOnTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LargeBear());
        harness.setHand(player1, List.of(new MomentOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Moment of Glory");
    }

    @Test
    void flashbackAlsoCountersOtherCreaturesYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LargeBear());
        harness.setGraveyard(player1, List.of(new MomentOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Moment of Glory");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Moment of Glory"));
    }

    @Test
    void cannotTargetCreatureAnOpponentControls() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LargeBear());
        harness.setHand(player1, List.of(new MomentOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void flashbackDoesNothingWhenItsTargetLeavesTheBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        harness.setGraveyard(player1, List.of(new MomentOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Moment of Glory");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Moment of Glory"));
    }

    @Test
    void flashbackAffectsCreaturesPresentAtResolutionButNotLands() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setGraveyard(player1, List.of(new MomentOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, target.getId());
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new LargeBear());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newcomer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetNoncreaturePermanentYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new MomentOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashbackCannotTargetCreatureAnOpponentControls() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new LargeBear());
        harness.setGraveyard(player1, List.of(new MomentOfGlory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
