package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LabyrinthOfSkophos.class, NyxbornCourser.class})
class LabyrinthOfSkophosTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability adds one colorless mana")
    void manaAbilityAddsColorlessMana() {
        Permanent labyrinth = harness.addToBattlefieldAndReturn(player1, new LabyrinthOfSkophos());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(labyrinth.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removes an attacking creature from combat")
    void removesAttackerFromCombat() {
        harness.addToBattlefield(player1, new LabyrinthOfSkophos());
        Permanent attacker = addAttacker(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isAttacking()).isFalse();
        assertThat(attacker.getAttackTarget()).isNull();
    }

    @Test
    @DisplayName("Removes a blocking creature from combat")
    void removesBlockerFromCombat() {
        harness.addToBattlefield(player1, new LabyrinthOfSkophos());
        Permanent blocker = addBlocker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(blocker.getBlockingTargetIds()).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player1, new LabyrinthOfSkophos());
        Permanent creature = addCreatureReady(player2, new NyxbornCourser());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing a tapped attacker does not untap it or let it deal combat damage")
    void removedAttackerRemainsTappedAndDealsNoDamage() {
        Permanent labyrinth = harness.addToBattlefieldAndReturn(player1, new LabyrinthOfSkophos());
        Permanent attacker = addAttacker(player2);
        attacker.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());

        assertThat(labyrinth.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(attacker.isAttacking()).isTrue();

        harness.passBothPriorities();
        harness.resolveCombatDamage();

        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.isAttacking()).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Removing the sole blocker leaves the attacker blocked and unable to damage the player")
    void removingSoleBlockerLeavesAttackerBlocked() {
        harness.addToBattlefield(player1, new LabyrinthOfSkophos());
        Permanent attacker = addAttacker(player2);
        Permanent blocker = addCreatureReady(player1, new NyxbornCourser());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(attacker.isAttacking()).isTrue();
        assertThat(attacker.isBlockedWithoutBlockers()).isTrue();

        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = addCreatureReady(player, new NyxbornCourser());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent blocker = addCreatureReady(player, new NyxbornCourser());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
