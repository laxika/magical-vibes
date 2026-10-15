package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionWhite;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteScarab.class, CircleOfProtectionWhite.class, BalduvianBears.class, KjeldoranWarrior.class})
class WhiteScarabTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can't be blocked by a white creature")
    void cannotBeBlockedByWhiteCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new KjeldoranWarrior());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a non-white creature")
    void canBeBlockedByNonWhiteCreature() {
        Permanent attacker = addCreatureReady(player1, new BalduvianBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("No boost when no opponent controls a white permanent")
    void noBoostWithoutOpponentWhitePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent controls a white permanent")
    void boostedWhenOpponentControlsWhitePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new KjeldoranWarrior());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost for an opponent's non-white permanent")
    void notBoostedWhenOpponentControlsNonWhitePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new BalduvianBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost uses the Aura controller's opponents")
    void boostUsesAuraControllerForOpponentCondition() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new WhiteScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new KjeldoranWarrior());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost ends when the opponent's white permanent leaves the battlefield")
    void boostEndsWhenOpponentWhitePermanentLeavesBattlefield() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(bears.getId());

        Permanent whitePermanent = harness.addToBattlefieldAndReturn(player2, new KjeldoranWarrior());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player2.getId()).remove(whitePermanent);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets +2/+2 when an opponent controls a white noncreature permanent")
    void boostedWhenOpponentControlsWhiteNoncreaturePermanent() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player2, new CircleOfProtectionWhite());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Controller's own white permanent does not grant the boost")
    void ownWhitePermanentDoesNotBoost() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        aura.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new KjeldoranWarrior());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting the Aura attaches it to an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        harness.setHand(player1, List.of(new WhiteScarab()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "White Scarab").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Scarabs each grant one boost regardless of the number of white permanents")
    void multipleScarabsStackButWhitePermanentsDoNotMultiplyBoost() {
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        first.setAttachedTo(bears.getId());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        second.setAttachedTo(bears.getId());
        harness.addToBattlefield(player2, new KjeldoranWarrior());
        harness.addToBattlefield(player2, new CircleOfProtectionWhite());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }
}
