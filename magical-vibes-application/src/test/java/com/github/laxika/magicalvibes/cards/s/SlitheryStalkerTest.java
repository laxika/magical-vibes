package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BarbarianOutcast;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.cards.m.MysticFamiliar;
import com.github.laxika.magicalvibes.cards.n.Narcissism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlitheryStalker.class, SetonsScout.class, MysticFamiliar.class, BarbarianOutcast.class,
        FieryTemper.class, Narcissism.class, Swamp.class})
class SlitheryStalkerTest extends BaseCardTest {

    @Test
    void etbExilesGreenCreature() {
        harness.addToBattlefield(player2, new SetonsScout());
        UUID targetId = harness.getPermanentId(player2, "Seton's Scout");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Seton's Scout");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Seton's Scout"));
    }

    @Test
    void etbExilesWhiteCreature() {
        harness.addToBattlefield(player2, new MysticFamiliar());
        UUID targetId = harness.getPermanentId(player2, "Mystic Familiar");

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Mystic Familiar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Mystic Familiar"));
    }

    @Test
    void exiledCreatureReturnsWhenSlitheryStalkerLeaves() {
        harness.addToBattlefield(player2, new SetonsScout());
        UUID targetId = harness.getPermanentId(player2, "Seton's Scout");
        castAndResolve(targetId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID sourceId = harness.getPermanentId(player1, "Slithery Stalker");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertOnBattlefield(player2, "Seton's Scout");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Seton's Scout"));
    }

    @Test
    void cannotTargetRedCreature() {
        harness.addToBattlefield(player2, new BarbarianOutcast());
        UUID targetId = harness.getPermanentId(player2, "Barbarian Outcast");
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureYouControl() {
        harness.addToBattlefield(player1, new SetonsScout());
        UUID targetId = harness.getPermanentId(player1, "Seton's Scout");
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetGreenNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Narcissism());
        UUID targetId = harness.getPermanentId(player2, "Narcissism");
        prepareToCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void etbExilesTargetIfSlitheryStalkerLeavesBeforeEtbResolves() {
        harness.addToBattlefield(player2, new SetonsScout());
        UUID targetId = harness.getPermanentId(player2, "Seton's Scout");

        prepareToCast();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        UUID sourceId = harness.getPermanentId(player1, "Slithery Stalker");
        harness.setHand(player2, List.of(new FieryTemper()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, sourceId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Slithery Stalker");
        harness.assertNotOnBattlefield(player2, "Seton's Scout");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Seton's Scout"));
    }

    @Test
    void swampwalkPreventsBlockingWhenDefenderControlsSwamp() {
        Permanent attacker = addCreatureReady(player1, new SlitheryStalker());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = addCreatureReady(player2, new SetonsScout());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void swampwalkAllowsBlockingWhenDefenderControlsNoSwamp() {
        Permanent attacker = addCreatureReady(player1, new SlitheryStalker());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SetonsScout());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private void castAndResolve(UUID targetId) {
        prepareToCast();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareToCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SlitheryStalker()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
