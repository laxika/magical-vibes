package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.s.SamiteArcher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Restrain.class, SamiteArcher.class, HolyDay.class, Repulse.class})
class RestrainTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage from only the targeted attacker and draws a card")
    void preventsTargetedCombatDamageAndDraws() {
        harness.setLife(player2, 20);
        Permanent restrainedAttacker = addAttacker(player1, player2, 2, 2);
        addAttacker(player1, player2, 3, 3);
        harness.setLibrary(player2, List.of(new HolyDay()));

        castRestrain(restrainedAttacker);

        harness.assertInHand(player2, "Holy Day");
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Prevents only combat damage; the targeted creature can still deal noncombat damage")
    void onlyPreventsCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addAttacker(player1, player2, 2, 2);
        harness.setLibrary(player2, List.of(new HolyDay()));

        castRestrain(attacker);

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        harness.activateAbility(player1, attackerIndex, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        resolveCombat();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttacker() {
        Permanent bystander = harness.addToBattlefieldAndReturn(player1, new SamiteArcher());
        prepareRestrain();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw when the target leaves the battlefield before resolution")
    void doesNotDrawWhenTargetLeavesBattlefield() {
        Permanent attacker = addAttacker(player1, player2, 1, 1);
        harness.setLibrary(player1, List.of(new HolyDay()));
        harness.setLibrary(player2, List.of(new HolyDay()));
        prepareRestrain();
        harness.castInstant(player2, 0, attacker.getId());

        harness.setHand(player1, List.of(new Repulse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Samite Archer");
        harness.assertNotInHand(player2, "Holy Day");
        harness.assertInGraveyard(player2, "Restrain");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when the target is no longer attacking at resolution")
    void doesNotDrawWhenTargetStopsAttacking() {
        Permanent attacker = addAttacker(player1, player2, 1, 1);
        harness.setLibrary(player2, List.of(new HolyDay()));
        prepareRestrain();
        harness.castInstant(player2, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Holy Day");
        harness.assertInGraveyard(player2, "Restrain");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws even when the attacking creature has zero power")
    void drawsEvenWhenNoDamageWouldBeDealt() {
        Permanent attacker = addAttacker(player1, player2, 0, 1);
        harness.setLibrary(player2, List.of(new HolyDay()));

        castRestrain(attacker);

        harness.assertInHand(player2, "Holy Day");
        harness.assertInGraveyard(player2, "Restrain");
    }

    @Test
    @DisplayName("Prevents damage to a blocker but does not protect the targeted attacker")
    void preventsDamageToBlockerButNotToAttacker() {
        Permanent attacker = addAttacker(player1, player2, 1, 1);
        Permanent blocker = addCreatureReady(player2, new SamiteArcher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLibrary(player2, List.of(new HolyDay()));

        castRestrain(attacker);
        resolveCombat();

        harness.assertInGraveyard(player1, "Samite Archer");
        harness.assertOnBattlefield(player2, "Samite Archer");
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    private void castRestrain(Permanent target) {
        prepareRestrain();
        harness.castAndResolveInstant(player2, 0, target.getId());
    }

    private void prepareRestrain() {
        harness.setHand(player2, List.of(new Restrain()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Card creature = new SamiteArcher();
        creature.setPower(power);
        creature.setToughness(toughness);
        Permanent perm = addCreatureReady(owner, creature);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }
}
