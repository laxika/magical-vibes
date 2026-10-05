package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DreamThrush;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MetathranTransport.class, DreamThrush.class, RazorfootGriffin.class, Island.class})
class MetathranTransportTest extends BaseCardTest {

    @Test
    @DisplayName("Metathran Transport can't be blocked by a blue creature")
    void cannotBeBlockedByBlueCreature() {
        Permanent blocker = addCreatureReady(player2, new DreamThrush());
        Permanent attacker = addCreatureReady(player1, new MetathranTransport());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Metathran Transport can be blocked by a non-blue creature")
    void canBeBlockedByNonBlueCreature() {
        Permanent blocker = addCreatureReady(player2, new RazorfootGriffin());
        Permanent attacker = addCreatureReady(player1, new MetathranTransport());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("{U}: target creature becomes blue, replacing its other colors")
    void targetBecomesBlue() {
        harness.addToBattlefield(player1, new MetathranTransport());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Blue wears off at end of turn")
    void blueWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MetathranTransport());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }

    @Test
    @DisplayName("Turning an opposing creature blue makes it unable to block")
    void turningOpposingCreatureBluePreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new MetathranTransport());
        Permanent blocker = addCreatureReady(player2, new RazorfootGriffin());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, blocker)).containsExactly(CardColor.BLUE);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new MetathranTransport());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, newly entered Transport can activate repeatedly without tapping")
    void tappedTransportCanActivateRepeatedly() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MetathranTransport());
        source.setTapped(true);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, first)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, second)).containsExactly(CardColor.BLUE);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("The ability requires blue mana even when generic mana is available")
    void cannotActivateWithoutBlueMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MetathranTransport());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(source.isTapped()).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
