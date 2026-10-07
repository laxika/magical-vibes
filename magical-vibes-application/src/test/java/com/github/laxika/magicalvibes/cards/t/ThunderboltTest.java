package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.n.NarstadScrapper;
import com.github.laxika.magicalvibes.cards.s.SeraphOfDawn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thunderbolt.class, SeraphOfDawn.class, NarstadScrapper.class, TamiyoTheMoonSage.class, Archangel.class})
class ThunderboltTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Mode 0 deals 3 damage to target player")
    void mode0DamagesPlayer() {
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Thunderbolt");
    }

    @Test
    @DisplayName("Mode 0 deals 3 damage to target planeswalker")
    void mode0DamagesPlaneswalker() {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoTheMoonSage());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castInstant(player1, 0, 0, tamiyo.getId());
        harness.passBothPriorities();

        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mode 1 deals 4 damage to a creature with flying, killing it")
    void mode1KillsFlyingCreature() {
        Permanent seraph = harness.addToBattlefieldAndReturn(player2, new SeraphOfDawn());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castModalInstant(player1, 0, 1, List.of(seraph.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Seraph of Dawn");
        harness.assertInGraveyard(player2, "Seraph of Dawn");
    }

    @Test
    @DisplayName("Mode 1 deals exactly 4 damage to a large flier")
    void mode1DealsFourDamage() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new Archangel());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castModalInstant(player1, 0, 1, List.of(angel.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Archangel");
        assertThat(angel.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Mode 1 cannot target a creature without flying")
    void mode1CannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new SeraphOfDawn());
        Permanent scrapper = harness.addToBattlefieldAndReturn(player2, new NarstadScrapper());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(scrapper.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode0CanTargetController() {
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    void mode0CannotTargetFlyingCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new Archangel());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1CannotTargetPlayer() {
        harness.addToBattlefield(player2, new SeraphOfDawn());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1CannotTargetPlaneswalker() {
        harness.addToBattlefield(player2, new SeraphOfDawn());
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoTheMoonSage());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(tamiyo.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mode1CanTargetOwnFlyingCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new Archangel());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castModalInstant(player1, 0, 1, List.of(angel.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Archangel");
        assertThat(angel.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void mode1DoesNotDamageTargetThatLosesFlying() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new Archangel());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castModalInstant(player1, 0, 1, List.of(angel.getId()));
        angel.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Archangel");
        assertThat(angel.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Thunderbolt");
    }
}
