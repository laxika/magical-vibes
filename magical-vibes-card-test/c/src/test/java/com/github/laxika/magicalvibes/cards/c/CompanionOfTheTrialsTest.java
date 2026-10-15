package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompanionOfTheTrials.class, GideonOfTheTrials.class, GrizzlyBears.class, Forest.class})
class CompanionOfTheTrialsTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps target creature when controlling a Gideon planeswalker")
    void untapsTargetWhenControllingGideon() {
        Permanent companion = addReadyCompanion(player1);
        addReadyGideon(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(companion), null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped creature")
    void canUntapOwnCreature() {
        Permanent companion = addReadyCompanion(player1);
        addReadyGideon(player1);
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        own.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(companion), null, own.getId());
        harness.passBothPriorities();

        assertThat(own.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a Gideon planeswalker")
    void cannotActivateWithoutGideon() {
        Permanent companion = addReadyCompanion(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(companion), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent companion = addReadyCompanion(player1);
        addReadyGideon(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(companion), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent companion = addReadyCompanion(player1);
        addReadyGideon(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(companion), null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Gideon does not allow activation")
    void opponentsGideonDoesNotAllowActivation() {
        Permanent companion = addReadyCompanion(player1);
        addReadyGideon(player2);
        companion.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(companion), null, companion.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(companion.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can untap itself while tapped and summoning sick")
    void canUntapItselfWhileSummoningSick() {
        Permanent companion = harness.addToBattlefieldAndReturn(player1, new CompanionOfTheTrials());
        companion.setSummoningSick(true);
        companion.tap();
        addReadyGideon(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(companion), null, companion.getId());
        harness.passBothPriorities();

        assertThat(companion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untap still resolves after the controlled Gideon leaves")
    void resolvesAfterGideonLeaves() {
        Permanent companion = addReadyCompanion(player1);
        Permanent gideon = addReadyGideon(player1);
        companion.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(companion), null, companion.getId());
        gd.playerBattlefields.get(player1.getId()).remove(gideon);
        gd.playerGraveyards.get(player1.getId()).add(gideon.getCard());
        harness.passBothPriorities();

        assertThat(companion.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an already untapped creature")
    void canTargetUntappedCreature() {
        Permanent companion = addReadyCompanion(player1);
        addReadyGideon(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(companion), null, companion.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(companion.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyCompanion(Player player) {
        return addCreatureReady(player, new CompanionOfTheTrials());
    }

    private Permanent addReadyGideon(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GideonOfTheTrials());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        return perm;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
