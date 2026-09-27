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
import java.util.UUID;

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
        harness.addToBattlefield(player1, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Razorfoot Griffin");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Razorfoot Griffin");
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Blue wears off at end of turn")
    void blueWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MetathranTransport());
        harness.addToBattlefield(player1, new RazorfootGriffin());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Razorfoot Griffin");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Razorfoot Griffin");
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
}
