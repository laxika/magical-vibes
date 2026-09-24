package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConsumeSpirit.class, RuneclawBear.class, ChandraNalaar.class, SafePassage.class})
class ConsumeSpiritTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Consume Spirit targeting a player puts it on the stack")
    void castingTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    // ===== Damage to player and life gain =====

    @Test
    @DisplayName("Consume Spirit deals X damage to target player and gains X life")
    void dealsXDamageToPlayerAndGainsXLife() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Damage to creature =====

    @Test
    @DisplayName("Consume Spirit deals X damage to target creature and gains X life")
    void dealsXDamageToCreatureAndGainsXLife() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 15);

        harness.castSorcery(player1, 0, 2, bear.getId());
        harness.passBothPriorities();

        // 2 damage kills Runeclaw Bear (2 toughness)
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        // Controller gains 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Consume Spirit fizzles when target creature is removed before resolution — no life gain")
    void fizzlesWhenTargetCreatureRemoved() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 15);

        harness.castSorcery(player1, 0, 2, bear.getId());
        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        // Spell fizzles — no damage and no life gain
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    // ===== Mana restriction: spend only black on X =====

    @Test
    @DisplayName("Cannot pay X with non-black mana")
    void cannotPayXWithNonBlackMana() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // With 2 black and 2 blue, the total pool can pay X=2, but only X=1 can use black mana
        // after the mandatory {B} is paid.
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        // X=2 requires 2 black for X + 1 black for {B} + 1 generic = 4 total.
        // The blue mana cannot be used for X.
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can pay X with only black mana and generic with any color")
    void canPayXWithBlackAndGenericWithAny() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // 3 black + 1 blue = {B} takes 1 black, {1} takes 1 blue, X can be 2 (from 2 remaining black)
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Can cast with X=0 for minimum cost")
    void canCastWithXZero() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // {X}{1}{B} with X=0 costs just {1}{B}
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        // X=0: 0 damage and 0 life gain
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Consume Spirit can target a planeswalker and gains X life")
    void dealsXDamageToPlaneswalkerAndGainsXLife() {
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Consume Spirit gains X life even when its damage is prevented")
    void gainsXLifeWhenDamageIsPrevented() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ConsumeSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot cast without enough mana for base cost")
    void cannotCastWithoutBaseMana() {
        harness.setHand(player1, List.of(new ConsumeSpirit()));
        // Only 1 mana, need at least 2 for {1}{B}
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}

