package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({MinasMorgulDarkFortress.class, GrizzlyBears.class})
class MinasMorgulDarkFortressTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and produces black mana")
    void entersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new MinasMorgulDarkFortress()));

        harness.playLand(player1, 0);
        Permanent fortress = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(fortress.isTapped()).isTrue();

        fortress.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a shadow counter on a creature and grants Wraith while it remains")
    void putsShadowCounterAndGrantsWraithWhileCounterRemains() {
        Permanent fortress = harness.addToBattlefieldAndReturn(player1, new MinasMorgulDarkFortress());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(fortress.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.SHADOW)).isEqualTo(1);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.WRAITH)).isTrue();

        creature.setCounterCount(CounterType.SHADOW, 0);
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.WRAITH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent fortress = harness.addToBattlefieldAndReturn(player1, new MinasMorgulDarkFortress());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new MinasMorgulDarkFortress());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(fortress.isTapped()).isFalse();
        assertThat(noncreature.getCounterCount(CounterType.SHADOW)).isZero();
    }
}
