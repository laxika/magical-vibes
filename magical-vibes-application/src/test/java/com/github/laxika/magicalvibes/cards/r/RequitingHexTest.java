package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RequitingHex.class, GrizzlyBears.class, HillGiant.class})
class RequitingHexTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private void cast(Permanent target, Permanent blightCreature) {
        harness.castInstantWithSacrifice(player1, 0, target.getId(),
                blightCreature == null ? null : blightCreature.getId());
    }

    @Test
    void destroysSmallCreatureWithoutBlightAndDoesNotGainLife() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RequitingHex()));
        addMana();
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void blightAddsCounterAndGainsLife() {
        Permanent blightCreature = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RequitingHex()));
        addMana();
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, blightCreature);

        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void cannotTargetCreatureWithManaValueGreaterThanTwo() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new RequitingHex()));
        addMana();

        assertThatThrownBy(() -> cast(target, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void canBlightTheCreatureBeingDestroyed() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RequitingHex()));
        addMana();
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, target);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void doesNotGainLifeWhenBlightKillsTheOnlyTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new RequitingHex()));
        addMana();
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, target);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void gainsLifeEvenIfTheBlightedCreatureDiesWhilePayingTheCost() {
        Permanent blightCreature = addCreatureReady(player1, new GrizzlyBears());
        blightCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RequitingHex()));
        addMana();
        int lifeBefore = gd.getLife(player1.getId());

        cast(target, blightCreature);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blightCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blightCreature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void cannotBlightAnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RequitingHex()));
        addMana();

        assertThatThrownBy(() -> cast(target, target))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).isEmpty();
    }
}
