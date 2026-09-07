package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourageInCrisis.class, GrizzlyBears.class, Spellbook.class})
class CourageInCrisisTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on target creature, then proliferates")
    void putsCounterThenProliferates() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);
        Permanent other = new Permanent(new GrizzlyBears());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(other);

        cast(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId(), other.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts the counter even when proliferate chooses none")
    void putsCounterWhenProliferateChoosesNone() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);
        Permanent other = new Permanent(new GrizzlyBears());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gd.playerBattlefields.get(player1.getId()).add(other);

        cast(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new CourageInCrisis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CourageInCrisis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
