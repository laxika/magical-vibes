package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloakOfMists.class, CoralMerfolk.class, WornPowerstone.class})
class CloakOfMistsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Cloak of Mists attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new CloakOfMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cloak of Mists")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cloak of Mists makes the enchanted creature unblockable")
    void enchantedCreatureCannotBeBlocked() {
        Permanent bears = addCreatureReady(player1, new CoralMerfolk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CloakOfMists());
        aura.setAttachedTo(bears.getId());
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        bears.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cloak of Mists stops applying when it leaves the battlefield")
    void effectStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new CoralMerfolk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CloakOfMists());
        aura.setAttachedTo(bears.getId());
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        bears.setAttacking(true);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cloak of Mists cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.setHand(player1, List.of(new CloakOfMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cloak of Mists can enchant an opponent's creature and prevents it from being blocked")
    void enchantsOpponentsCreature() {
        Permanent attacker = addCreatureReady(player2, new CoralMerfolk());
        Permanent blocker = addCreatureReady(player1, new CoralMerfolk());
        harness.setHand(player1, List.of(new CloakOfMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof CloakOfMists
                        && attacker.getId().equals(p.getAttachedTo()));
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        int blockerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Cloak of Mists does not prevent blocking another creature")
    void otherCreatureCanStillBeBlocked() {
        Permanent enchanted = addCreatureReady(player1, new CoralMerfolk());
        Permanent attacker = addCreatureReady(player1, new CoralMerfolk());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CloakOfMists());
        aura.setAttachedTo(enchanted.getId());
        Permanent blocker = addCreatureReady(player2, new CoralMerfolk());
        enchanted.setAttacking(true);
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
