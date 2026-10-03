package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CosmiumBlast.class, ColossalDreadmaw.class, GrizzlyBears.class})
class CosmiumBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to an attacking creature")
    void dealsDamageToAttackingCreature() {
        Permanent attacker = addCombatCreature(player2, new ColossalDreadmaw(), true);

        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Deals 4 damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        Permanent blocker = addCombatCreature(player2, new ColossalDreadmaw(), false);

        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CosmiumBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Lethal damage destroys an attacking creature")
    void lethalDamageDestroysAttacker() {
        Permanent attacker = addCombatCreature(player2, new GrizzlyBears(), true);

        castSpellAt(attacker.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cosmium Blast");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnBlockingCreature() {
        Permanent blocker = addCombatCreature(player1, new ColossalDreadmaw(), false);

        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Deals no damage when the target leaves combat before resolution")
    void targetLeavingCombatMakesSpellFailToResolve() {
        Permanent attacker = addCombatCreature(player2, new ColossalDreadmaw(), true);
        harness.setHand(player1, List.of(new CosmiumBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        harness.assertInGraveyard(player1, "Cosmium Blast");
    }

    private void castSpellAt(UUID targetId) {
        harness.setHand(player1, List.of(new CosmiumBlast()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    private Permanent addCombatCreature(Player owner, Card card, boolean attacking) {
        Permanent permanent = addCreatureReady(owner, card);
        if (attacking) {
            permanent.setAttacking(true);
            permanent.setAttackTarget(owner == player1 ? player2.getId() : player1.getId());
        } else {
            permanent.setBlocking(true);
            permanent.addBlockingTargetId(UUID.randomUUID());
        }
        return permanent;
    }
}
