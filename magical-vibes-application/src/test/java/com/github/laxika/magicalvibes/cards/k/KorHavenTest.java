package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AncientHydra;
import com.github.laxika.magicalvibes.cards.r.RootwaterCommando;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorHaven.class, RootwaterCommando.class, AncientHydra.class})
class KorHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new KorHaven());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage dealt by the target attacking creature")
    void preventsCombatDamageByTargetAttacker() {
        harness.setLife(player1, 20);
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        Permanent attacker = addAttacker(player2);

        prepareAttackStep();
        addPreventionMana();
        activatePrevention(haven, attacker);

        assertThat(haven.isTapped()).isTrue();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt by another attacking creature")
    void doesNotPreventCombatDamageByOtherAttacker() {
        harness.setLife(player1, 20);
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        Permanent protectedAttacker = addAttacker(player2);
        addAttacker(player2);

        prepareAttackStep();
        addPreventionMana();
        activatePrevention(haven, protectedAttacker);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Prevents combat damage dealt by the target attacker to a blocker")
    void preventsCombatDamageToBlocker() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        addCreatureReady(player1, new RootwaterCommando());
        Permanent attacker = addAttacker(player2);

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        addPreventionMana();
        activatePrevention(haven, attacker);
        resolveCombat(player2);

        Permanent blocker = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Rootwater Commando");
    }

    @Test
    @DisplayName("Does not prevent noncombat damage dealt by the target attacker")
    void doesNotPreventNoncombatDamageByTargetAttacker() {
        harness.setLife(player1, 20);
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        addCreatureReady(player1, new RootwaterCommando());
        Permanent attacker = harness.enterBattlefieldAndReturn(player2, new AncientHydra());
        attacker.setSummoningSick(false);

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 0, null, player1.getId());
        addPreventionMana();
        activatePrevention(haven, attacker);

        harness.passBothPriorities();
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        Permanent attacker = addAttacker(player2);
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new KorHaven());

        prepareAttackStep();
        addPreventionMana();
        activatePrevention(haven, attacker);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Does nothing if the target stops attacking before resolution")
    void doesNothingIfTargetStopsAttackingBeforeResolution() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        Permanent attacker = addAttacker(player2);

        prepareAttackStep();
        addPreventionMana();
        harness.activateAbility(player1, battlefieldIndex(player1, haven), 1, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        Permanent creature = addCreatureReady(player2, new RootwaterCommando());
        addPreventionMana();

        prepareAttackStep();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, haven), 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activatePrevention(Permanent haven, Permanent attacker) {
        harness.activateAbility(player1, battlefieldIndex(player1, haven), 1, null, attacker.getId());
        harness.passBothPriorities();
    }

    private void prepareAttackStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void addPreventionMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent attacker = addCreatureReady(owner, new RootwaterCommando());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
