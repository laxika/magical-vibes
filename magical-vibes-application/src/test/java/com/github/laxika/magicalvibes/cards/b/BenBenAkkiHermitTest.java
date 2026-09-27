package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AkkiAvalanchers;
import com.github.laxika.magicalvibes.cards.k.KokushoTheEveningStar;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenBenAkkiHermit.class, AkkiAvalanchers.class, KokushoTheEveningStar.class, Mountain.class})
class BenBenAkkiHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of untapped Mountains, killing the attacker")
    void dealsDamageEqualToUntappedMountains() {
        addBenBen(player1);
        addMountains(player1, 2, false);
        Permanent attacker = addAttackingCreature(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Akki Avalanchers");
    }

    @Test
    @DisplayName("Tapped Mountains are not counted")
    void tappedMountainsAreNotCounted() {
        addBenBen(player1);
        addMountains(player1, 1, false);
        addMountains(player1, 3, true);
        Permanent attacker = addAttackingKokusho(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Kokusho, the Evening Star");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("deals 1 damage"));
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addBenBen(player1);
        addMountains(player1, 2, false);
        Permanent perm = addCreatureReady(player2, new AkkiAvalanchers());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, perm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking");
    }

    @Test
    @DisplayName("Counts only the controller's untapped Mountains at resolution")
    void countsMountainsAtResolutionForController() {
        Permanent benBen = addBenBen(player1);
        Permanent firstMountain = addMountain(player1, false);
        addMountain(player1, false);
        addMountain(player1, true);
        addMountain(player2, false);
        Permanent attacker = addAttackingKokusho(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, battlefieldIndex(player1, benBen), 0, attacker.getId());
        firstMountain.tap();
        harness.passBothPriorities();

        assertThat(benBen.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an attacking noncreature")
    void cannotTargetAttackingNoncreature() {
        addBenBen(player1);
        addMountains(player1, 1, false);
        Permanent attackingMountain = addMountain(player2, false);
        attackingMountain.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, attackingMountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if the target stops attacking before resolution")
    void doesNothingIfTargetStopsAttacking() {
        addBenBen(player1);
        addMountains(player1, 2, false);
        Permanent attacker = addAttackingKokusho(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cannot activate while Ben-Ben has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent benBen = harness.addToBattlefieldAndReturn(player1, new BenBenAkkiHermit());
        Permanent attacker = addAttackingKokusho(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, benBen), 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addBenBen(Player player) {
        return addCreatureReady(player, new BenBenAkkiHermit());
    }

    private void addMountains(Player player, int count, boolean tapped) {
        for (int i = 0; i < count; i++) {
            addMountain(player, tapped);
        }
    }

    private Permanent addMountain(Player player, boolean tapped) {
        Permanent mountain = harness.addToBattlefieldAndReturn(player, new Mountain());
        if (tapped) {
            mountain.tap();
        }
        return mountain;
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent attacker = addCreatureReady(player, new AkkiAvalanchers());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addAttackingKokusho(Player player) {
        Permanent attacker = addCreatureReady(player, new KokushoTheEveningStar());
        attacker.setAttacking(true);
        return attacker;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
