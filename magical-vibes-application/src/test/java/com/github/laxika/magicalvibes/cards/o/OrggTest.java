package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GoblinHero;
import com.github.laxika.magicalvibes.cards.i.IronrootTreefolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Orgg.class, GoblinHero.class, IronrootTreefolk.class})
class OrggTest extends BaseCardTest {

    private Permanent orggReadyToAttack() {
        return addCreatureReady(player1, new Orgg());
    }

    // ===== Block restriction: can't block power 3 or greater =====

    @Test
    @DisplayName("Can block an attacker with power 2")
    void canBlockPowerTwo() {
        Permanent orgg = addCreatureReady(player1, new Orgg());
        Permanent goblinHero = addCreatureReady(player2, new GoblinHero()); // 2/2

        assertThat(bls.canBlockAttacker(gd, orgg, goblinHero,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    @Test
    @DisplayName("Can't block an attacker with power 3")
    void cantBlockPowerThree() {
        Permanent orgg = addCreatureReady(player1, new Orgg());
        Permanent ironrootTreefolk = addCreatureReady(player2, new IronrootTreefolk()); // 3/5

        assertThat(bls.canBlockAttacker(gd, orgg, ironrootTreefolk,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        orggReadyToAttack();
        Permanent goblinHero = addCreatureReady(player2, new GoblinHero()); // 2/2

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                goblinHero.getId(), 2,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(goblinHero);
    }

    // ===== Attack restriction: can't attack if defender has untapped power-3 creature =====

    @Test
    @DisplayName("Can't attack if defending player controls an untapped creature with power 3")
    void cantAttackWhenDefenderControlsUntappedPowerThree() {
        orggReadyToAttack();
        addCreatureReady(player2, new IronrootTreefolk()); // 3/5 untapped

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack if defending player's only power-3 creature is tapped")
    void canAttackWhenDefenderPowerThreeCreatureIsTapped() {
        orggReadyToAttack();
        Permanent tappedTreefolk = addCreatureReady(player2, new IronrootTreefolk()); // 3/5
        tappedTreefolk.tap();

        assertThatCode(() -> declareAttackers(List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Can attack if defending player controls only creatures with power less than 3")
    void canAttackWhenDefenderControlsOnlyLowPowerCreature() {
        orggReadyToAttack();
        addCreatureReady(player2, new GoblinHero()); // 2/2

        assertThatCode(() -> declareAttackers(List.of(0)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Can attack when only the attacking player controls a power-3 creature")
    void canAttackWhenAttackerControlsPowerThreeCreature() {
        orggReadyToAttack();
        addCreatureReady(player1, new IronrootTreefolk()); // 3/5 controlled by the attacker

        assertThatCode(() -> declareAttackers(List.of(0)))
                .doesNotThrowAnyException();
    }
}
