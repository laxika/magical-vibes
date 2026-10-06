package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilkbindFaerie.class, SafeholdElite.class, Forest.class})
class SilkbindFaerieTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W/U} and untapping taps target creature")
    void tapsTargetCreatureAndUntapsSource() {
        Permanent faerie = addTapped(player1, new SilkbindFaerie());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(faerie.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        // Paying {Q} untapped the source.
        assertThat(faerie.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it to be tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new SilkbindFaerie());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not tapped");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTapped(player1, new SilkbindFaerie());
        Permanent land = addCreatureReady(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Blue mana can pay the hybrid activation cost")
    void canPayWithBlueMana() {
        Permanent faerie = addTapped(player1, new SilkbindFaerie());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped summoning-sick Faerie cannot pay the untap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent faerie = harness.addToBattlefieldAndReturn(player1, new SilkbindFaerie());
        faerie.tap();
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(faerie.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Faerie may target itself and becomes tapped again on resolution")
    void canTargetItself() {
        Permanent faerie = addTapped(player1, new SilkbindFaerie());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, faerie.getId());
        assertThat(faerie.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent faerie = addTapped(player1, new SilkbindFaerie());
        Permanent target = addTapped(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An illegal target on resolution does not undo the untap cost")
    void targetLeavingBattlefieldDoesNotRefundUntapCost() {
        Permanent faerie = addTapped(player1, new SilkbindFaerie());
        Permanent target = addCreatureReady(player2, new SafeholdElite());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(faerie.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = addCreatureReady(player, card);
        perm.tap();
        return perm;
    }
}
