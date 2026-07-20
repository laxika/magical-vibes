package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImpeccableTimingTest extends BaseCardTest {

    // ===== Damage to an attacker =====

    @Test
    @DisplayName("Deals 3 damage to a target attacking creature")
    void dealsThreeToAttacker() {
        Permanent attacker = addAttacker(player2, new AirElemental(), "Air Elemental"); // 4/4
        castSpellAt(attacker.getId());

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Lethal damage destroys the attacking creature")
    void lethalDestroysAttacker() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears(), "Grizzly Bears"); // 2/2
        castSpellAt(attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Damage to a blocker =====

    @Test
    @DisplayName("Deals 3 damage to a target blocking creature")
    void dealsThreeToBlocker() {
        Permanent blocker = addBlocker(player2, new AirElemental(), "Air Elemental"); // 4/4
        castSpellAt(blocker.getId());

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    // ===== Target restrictions =====

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

    // ===== Helpers =====

    private void castSpellAt(UUID targetId) {
        harness.setHand(player1, List.of(new ImpeccableTiming()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, Card card, String name) {
        Permanent attacker = combatCreature(owner, card, name);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, Card card, String name) {
        Permanent blocker = combatCreature(owner, card, name);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }

    private Permanent combatCreature(Player owner, Card card, String name) {
        harness.addToBattlefield(owner, card);
        Permanent permanent = harness.getGameData().playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
        permanent.setSummoningSick(false);
        return permanent;
    }
}
