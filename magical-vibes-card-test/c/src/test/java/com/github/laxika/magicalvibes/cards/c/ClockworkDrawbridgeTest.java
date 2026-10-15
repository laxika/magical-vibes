package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClockworkDrawbridge.class, Forest.class})
class ClockworkDrawbridgeTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{W}, {T} taps target creature")
    void abilityTapsTargetCreature() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(drawbridge.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The ability can tap a creature you control")
    void tapsFriendlyCreature() {
        addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player1, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already tapped creature is a legal target")
    void canTargetTappedCreature() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(drawbridge.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Clockwork Drawbridge may target itself")
    void canTargetItself() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, drawbridge.getId());
        harness.passBothPriorities();

        assertThat(drawbridge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWithSummoningSickness() {
        Permanent drawbridge = harness.addToBattlefieldAndReturn(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(drawbridge.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Drawbridge cannot pay the tap cost again")
    void cannotActivateWhileTapped() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        drawbridge.tap();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three generic mana cannot replace the required white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drawbridge.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability needs two generic mana in addition to white")
    void cannotActivateWithInsufficientMana() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drawbridge.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(drawbridge);
        gd.playerGraveyards.get(player1.getId()).add(drawbridge.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The source taps as a cost and the target taps only on resolution")
    void targetWaitsForResolution() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(drawbridge.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability does nothing when its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());
        Permanent target = addCreatureReady(player2, new ClockworkDrawbridge());
        Permanent otherCreature = addCreatureReady(player2, new ClockworkDrawbridge());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(drawbridge.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Defender prevents attacking even after summoning sickness ends")
    void defenderCannotAttack() {
        Permanent drawbridge = addCreatureReady(player1, new ClockworkDrawbridge());

        assertThat(als.canAttack(gd, drawbridge, player1.getId())).isFalse();
    }
}
