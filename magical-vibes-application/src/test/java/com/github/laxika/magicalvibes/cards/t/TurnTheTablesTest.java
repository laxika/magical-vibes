package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BarbedLightning;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DroolingOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurnTheTables.class, BarbedLightning.class, CrazedGoblin.class, DroolingOgre.class})
class TurnTheTablesTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects combat damage to the target attacking creature")
    void redirectsCombatDamageToTargetAttackingCreature() {
        Permanent attacker = addCreatureReady(player2, new CrazedGoblin());
        Permanent target = addCreatureReady(player2, new DroolingOgre());
        Permanent blocker = addCreatureReady(player1, new CrazedGoblin());
        attacker.setAttacking(true);
        target.setAttacking(true);

        castTurnTheTables(target);
        int lifeBefore = gd.getLife(player1.getId());

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(target))));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(attacker.getMarkedDamage()).isEqualTo(0);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("Does not redirect noncombat damage")
    void doesNotRedirectNoncombatDamage() {
        Permanent target = addCreatureReady(player2, new DroolingOgre());
        target.setAttacking(true);
        castTurnTheTables(target);

        harness.setHand(player2, List.of(new BarbedLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{1}, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(target.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Requires an attacking creature target")
    void requiresAttackingCreatureTarget() {
        Permanent target = addCreatureReady(player2, new DroolingOgre());
        harness.setHand(player1, List.of(new TurnTheTables()));
        addTurnTheTablesMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires the target to still be attacking when the spell resolves")
    void requiresTargetToStillBeAttackingWhenSpellResolves() {
        Permanent target = addCreatureReady(player2, new DroolingOgre());
        target.setAttacking(true);

        harness.setHand(player1, List.of(new TurnTheTables()));
        addTurnTheTablesMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        harness.passBothPriorities();

        target.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Redirects separate combat damage events to the same creature")
    void redirectsSeparateCombatDamageEvents() {
        Permanent firstAttacker = addCreatureReady(player2, new CrazedGoblin());
        Permanent secondAttacker = addCreatureReady(player2, new CrazedGoblin());
        Permanent target = addCreatureReady(player2, new DroolingOgre());
        Permanent blocker = addCreatureReady(player1, new CrazedGoblin());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        target.setAttacking(true);

        castTurnTheTables(target);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(target))));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("The redirect expires at the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new DroolingOgre());
        target.setAttacking(true);
        castTurnTheTables(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        target.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    private void castTurnTheTables(Permanent target) {
        harness.setHand(player1, List.of(new TurnTheTables()));
        addTurnTheTablesMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addTurnTheTablesMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
