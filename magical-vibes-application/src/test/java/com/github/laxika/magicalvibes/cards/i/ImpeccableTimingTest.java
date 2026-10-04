package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpeccableTiming.class, AirElemental.class, GrizzlyBears.class})
class ImpeccableTimingTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target attacking creature")
    void dealsThreeToAttacker() {
        Permanent attacker = addAttacker(player2, new AirElemental()); // 4/4
        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal damage destroys the attacking creature")
    void lethalDestroysAttacker() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears()); // 2/2
        castSpellAt(attacker.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 3 damage to a target blocking creature")
    void dealsThreeToBlocker() {
        Permanent blocker = addBlocker(player2, new AirElemental()); // 4/4
        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ImpeccableTiming()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not damage a target that stopped attacking before resolution")
    void targetRemovedFromCombatIsIllegalOnResolution() {
        Permanent attacker = addAttacker(player2, new AirElemental());
        harness.setHand(player1, List.of(new ImpeccableTiming()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Impeccable Timing");
    }

    @Test
    @DisplayName("Can damage a creature controlled by the caster")
    void canTargetOwnAttacker() {
        Permanent attacker = addAttacker(player1, new AirElemental());
        attacker.setAttackTarget(player2.getId());

        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal damage destroys a blocking creature")
    void lethalDestroysBlocker() {
        Permanent blocker = addBlocker(player2, new GrizzlyBears());

        castSpellAt(blocker.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castSpellAt(UUID targetId) {
        harness.setHand(player1, List.of(new ImpeccableTiming()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Card card) {
        Permanent blocker = addCreatureReady(owner, card);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }

}
