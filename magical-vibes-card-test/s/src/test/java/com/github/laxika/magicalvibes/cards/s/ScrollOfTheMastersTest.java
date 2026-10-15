package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrollOfTheMasters.class, Spellbook.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("An opponent's noncreature spell does not add a lore counter")
    void opponentSpellDoesNotAddLoreCounter() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Spellbook()));

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(scroll.getCounterCount(CounterType.LORE)).isZero();
    }

    @Test
    @DisplayName("Resolving the boost before a pending lore trigger gives no bonus retroactively")
    void pendingLoreCounterDoesNotBoostCreatureRetroactively() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        harness.passBothPriorities();

        assertThat(scroll.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The bonus ends at cleanup but lore counters remain")
    void bonusExpiresAtEndOfTurn() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        scroll.setCounterCount(CounterType.LORE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);

        harness.passUntil(TurnStep.UNTAP);

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(scroll.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activation taps the artifact and cannot be repeated while it is tapped")
    void activationRequiresUntappedScroll() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, bears.getId());

        assertThat(scroll.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two mana cannot pay the activation's three-mana cost")
    void activationRequiresThreeMana() {
        harness.addToBattlefieldAndReturn(player1, new ScrollOfTheMasters());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
