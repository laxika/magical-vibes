package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RangerEnVecTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the {G} ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addRangerReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ranger = findPermanent(player1, "Ranger en-Vec");
        assertThat(ranger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Ranger en-Vec from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent perm = addRangerReady(player1);
        perm.setRegenerationShield(1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ranger en-Vec");
        Permanent ranger = findPermanent(player1, "Ranger en-Vec");
        assertThat(ranger.isTapped()).isTrue();
        assertThat(ranger.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ranger en-Vec dies to lethal combat damage without a shield")
    void diesWithoutRegenShield() {
        Permanent perm = addRangerReady(player1);
        perm.setBlocking(true);
        perm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ranger en-Vec");
        harness.assertInGraveyard(player1, "Ranger en-Vec");
    }

    @Test
    @DisplayName("First strike kills the blocker before it deals damage back")
    void firstStrikeKillsBlockerFirst() {
        Permanent ranger = addRangerReady(player1);
        ranger.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, 2, 2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ranger en-Vec");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent addRangerReady(Player player) {
        Permanent perm = new Permanent(new RangerEnVec());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
