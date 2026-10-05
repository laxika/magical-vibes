package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BenalishHero;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({MountainYeti.class, Mountain.class, BenalishHero.class, DAvenantArcher.class, SpiritLink.class})
class MountainYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Mountain Yeti cannot be blocked when defending player controls a Mountain")
    void cannotBeBlockedWhenDefenderControlsMountain() {
        harness.addToBattlefield(player2, new Mountain());

        Permanent blockerPerm = addCreatureReady(player2, new MountainYeti());

        Permanent atkPerm = addCreatureReady(player1, new MountainYeti());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Mountain Yeti can be blocked when defending player does not control a Mountain")
    void canBeBlockedWhenDefenderDoesNotControlMountain() {
        Permanent blockerPerm = addCreatureReady(player2, new MountainYeti());

        Permanent atkPerm = addCreatureReady(player1, new MountainYeti());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mountain Yeti can be blocked when only the attacking player controls a Mountain")
    void canBeBlockedWhenOnlyAttackerControlsMountain() {
        harness.addToBattlefield(player1, new Mountain());

        Permanent blockerPerm = addCreatureReady(player2, new MountainYeti());
        Permanent atkPerm = addCreatureReady(player1, new MountainYeti());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blockerPerm);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(atkPerm);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Mountain Yeti has protection from white but not red")
    void hasProtectionFromWhiteOnly() {
        Permanent yeti = harness.addToBattlefieldAndReturn(player1, new MountainYeti());

        assertThat(gqs.hasProtectionFrom(gd, yeti, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, yeti, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("A white creature cannot block Mountain Yeti")
    void whiteCreatureCannotBlockMountainYeti() {
        addCreatureReady(player2, new BenalishHero());
        Permanent atkPerm = addCreatureReady(player1, new MountainYeti());
        atkPerm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Mountain Yeti may block a white creature and prevents its combat damage")
    void preventsWhiteCombatDamageWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new DAvenantArcher());
        attacker.setAttacking(true);
        Permanent yeti = addCreatureReady(player2, new MountainYeti());
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player2, "Mountain Yeti");
        assertThat(yeti.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "D'Avenant Archer");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection from white does not prevent red combat damage")
    void doesNotPreventRedCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new MountainYeti());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MountainYeti());
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();

        harness.assertInGraveyard(player1, "Mountain Yeti");
        harness.assertInGraveyard(player2, "Mountain Yeti");
    }

    @Test
    @DisplayName("A white activated ability cannot target Mountain Yeti")
    void whiteAbilityCannotTargetYeti() {
        Permanent yeti = addCreatureReady(player1, new MountainYeti());
        yeti.setAttacking(true);
        Permanent archer = addCreatureReady(player2, new DAvenantArcher());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, yeti.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");

        assertThat(archer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Even its controller cannot target Mountain Yeti with a white Aura")
    void controllerCannotEnchantYetiWithWhiteAura() {
        Permanent yeti = addCreatureReady(player1, new MountainYeti());
        addCreatureReady(player1, new BenalishHero());
        harness.setHand(player1, List.of(new SpiritLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, yeti.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Spirit Link");
    }

    @Test
    @DisplayName("A white Aura attached without targeting is removed by protection")
    void whiteAuraCannotRemainAttached() {
        Permanent yeti = harness.addToBattlefieldAndReturn(player1, new MountainYeti());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpiritLink());
        aura.setAttachedTo(yeti.getId());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Mountain Yeti");
        harness.assertNotOnBattlefield(player1, "Spirit Link");
        harness.assertInGraveyard(player1, "Spirit Link");
    }
}
