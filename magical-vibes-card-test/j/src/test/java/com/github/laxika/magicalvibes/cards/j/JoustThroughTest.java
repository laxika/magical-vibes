package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({JoustThrough.class, AirElemental.class, GrizzlyBears.class})
class JoustThroughTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to an attacking creature and gains 1 life")
    void dealsDamageToAttackerAndGainsLife() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), "Air Elemental", true);

        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Deals 3 damage to a blocking creature and gains 1 life")
    void dealsDamageToBlockerAndGainsLife() {
        Permanent blocker = addCombatCreature(player2, new AirElemental(), "Air Elemental", false);

        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JoustThrough()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Lethal damage kills the target and still gains life")
    void lethalDamageStillGainsLife() {
        Permanent attacker = addCombatCreature(player2, new GrizzlyBears(), "Grizzly Bears", true);

        castSpellAt(attacker.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target your own blocking creature")
    void canTargetOwnBlocker() {
        Permanent blocker = addCombatCreature(player1, new AirElemental(), "Air Elemental", false);

        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when the target leaves combat before resolution")
    void noLifeGainWhenTargetLeavesCombat() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), "Air Elemental", true);
        harness.setHand(player1, List.of(new JoustThrough()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Joust Through");
    }

    @Test
    @DisplayName("Does not gain life when the target leaves the battlefield before resolution")
    void noLifeGainWhenTargetLeavesBattlefield() {
        Permanent attacker = addCombatCreature(player2, new AirElemental(), "Air Elemental", true);
        harness.setHand(player1, List.of(new JoustThrough()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, attacker.getId());

        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerGraveyards.get(player2.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Joust Through");
    }

    private void castSpellAt(UUID targetId) {
        harness.setHand(player1, List.of(new JoustThrough()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private Permanent addCombatCreature(Player owner, Card card, String name, boolean attacking) {
        harness.addToBattlefield(owner, card);
        Permanent permanent = findPermanent(owner, name);
        permanent.setSummoningSick(false);
        if (attacking) {
            permanent.setAttacking(true);
            permanent.setAttackTarget(player1.getId());
        } else {
            permanent.setBlocking(true);
            permanent.addBlockingTargetId(UUID.randomUUID());
        }
        return permanent;
    }
}
