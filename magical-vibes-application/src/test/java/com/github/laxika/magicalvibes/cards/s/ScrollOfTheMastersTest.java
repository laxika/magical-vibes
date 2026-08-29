package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScrollOfTheMastersTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts a lore counter on Scroll of the Masters")
    void noncreatureSpellAddsLoreCounter() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(scroll.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put a lore counter on Scroll of the Masters")
    void creatureSpellDoesNotAddLoreCounter() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(scroll.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    @DisplayName("The activated ability boosts a creature by the number of lore counters")
    void activatedAbilityBoostsByLoreCounters() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        scroll.setCounterCount(CounterType.LORE, 3);
        bears.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability cannot target an opponent's creature")
    void activatedAbilityCannotTargetOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
