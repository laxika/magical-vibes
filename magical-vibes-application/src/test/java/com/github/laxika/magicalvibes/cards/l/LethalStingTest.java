package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LethalStingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on a creature you control as a cost, then destroys target creature")
    void putsCounterAsCostThenDestroys() {
        Permanent counterBearer = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(counterBearer);
        Permanent victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new LethalSting()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorceryWithSacrifice(player1, 0, victim.getId(), counterBearer.getId());

        // The -1/-1 counter is placed immediately as part of paying the cost.
        assertThat(counterBearer.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hill Giant"));
    }

    @Test
    @DisplayName("Cannot be cast without a creature you control to receive the counter")
    void cannotCastWithoutControlledCreature() {
        Permanent victim = new Permanent(new HillGiant());
        gd.playerBattlefields.get(player2.getId()).add(victim);
        harness.setHand(player1, List.of(new LethalSting()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, victim.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent counterBearer = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(counterBearer);
        harness.addToBattlefield(player2, new Forest());
        var land = harness.getPermanentId(player2, "Forest");
        harness.setHand(player1, List.of(new LethalSting()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land, counterBearer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
