package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LooterIlKor;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.cards.s.SpiketailDrakeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherWeb.class, AshcoatBear.class, LooterIlKor.class, PrismaticLens.class, SpiketailDrakeling.class})
class AetherWebTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1, reach, and can block creatures with shadow")
    void enchantedCreatureGetsBoostReachAndShadowBlocking() {
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        enchant(blocker, player2);
        Permanent attacker = addCreatureReady(player1, new LooterIlKor());
        attacker.setAttacking(true);

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.REACH)).isTrue();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature loses Aether Web's effects when the Aura leaves")
    void effectsLostWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        Permanent aura = enchant(creature, player1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();

        Permanent attacker = addCreatureReady(player2, new LooterIlKor());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("Only the enchanted creature can block creatures with shadow")
    void shadowBlockingPermissionIsLimitedToEnchantedCreature() {
        Permanent unenchantedBlocker = addCreatureReady(player2, new AshcoatBear());
        Permanent enchantedBlocker = addCreatureReady(player2, new AshcoatBear());
        enchant(enchantedBlocker, player2);
        Permanent attacker = addCreatureReady(player1, new LooterIlKor());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
        assertThat(unenchantedBlocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Resolving Aether Web attaches it to a creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new AetherWeb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Aether Web");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());
        harness.setHand(player1, List.of(new AetherWeb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An enchanted shadow creature cannot block another shadow creature")
    void enchantedShadowCreatureCannotBlockShadowAttacker() {
        Permanent blocker = addCreatureReady(player2, new LooterIlKor());
        enchant(blocker, player2);
        Permanent attacker = addCreatureReady(player1, new LooterIlKor());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("An enchanted shadow creature cannot block a creature without shadow")
    void enchantedShadowCreatureCannotBlockNonShadowAttacker() {
        Permanent blocker = addCreatureReady(player2, new LooterIlKor());
        enchant(blocker, player2);
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    @DisplayName("The enchanted creature can still block creatures without shadow")
    void enchantedCreatureCanBlockNonShadowAttacker() {
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        enchant(blocker, player2);
        Permanent attacker = addCreatureReady(player1, new AshcoatBear());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Granted reach allows blocking a flying creature")
    void enchantedCreatureCanBlockFlyingAttacker() {
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());
        enchant(blocker, player2);
        Permanent attacker = addCreatureReady(player1, new SpiketailDrakeling());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Flash allows casting Aether Web during the opponent's combat")
    void canCastDuringOpponentsCombat() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new AetherWeb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Aether Web").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    private Permanent enchant(Permanent creature, Player owner) {
        Permanent aura = harness.addToBattlefieldAndReturn(owner, new AetherWeb());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
