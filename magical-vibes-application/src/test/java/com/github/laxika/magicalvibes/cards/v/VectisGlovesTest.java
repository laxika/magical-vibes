package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.g.GoldmireBridge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MyrScrapling;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({VectisGloves.class, GrizzlyBears.class, AncientDen.class, Ornithopter.class,
        Mountain.class, DressDown.class, GoldmireBridge.class, MyrScrapling.class})
class VectisGlovesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsPowerBonus() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gloves.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifact landwalk prevents blocking while defending player controls an artifact land")
    void artifactLandwalkPreventsBlocking() {
        harness.addToBattlefield(player2, new AncientDen());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = equippedAttacker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Artifact landwalk does not apply to a nonland artifact")
    void nonlandArtifactDoesNotGrantArtifactLandwalk() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = equippedAttacker();
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Equip attaches Vectis Gloves to another creature")
    void equipAttachesToAnotherCreature() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gloves.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent equippedAttacker() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        gloves.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        return attacker;
    }

    @Test
    @DisplayName("An artifact and a separate nonartifact land do not enable artifact landwalk")
    void separateArtifactAndLandDoNotPreventBlocking() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Mountain());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = equippedAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    @Test
    @DisplayName("An artifact land controlled by the attacking player does not prevent blocking")
    void attackersArtifactLandDoesNotPreventBlocking() {
        harness.addToBattlefield(player1, new AncientDen());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = equippedAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    @Test
    @DisplayName("Re-equipping transfers the power bonus and artifact landwalk")
    void reequippingTransfersBothBenefits() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent former = addCreatureReady(player1, new GrizzlyBears());
        Permanent current = addCreatureReady(player1, new GrizzlyBears());
        gloves.setAttachedTo(former.getId());
        harness.addToBattlefield(player2, new AncientDen());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, current.getId());
        harness.passBothPriorities();

        assertThat(gloves.getAttachedTo()).isEqualTo(current.getId());
        assertThat(gqs.getEffectivePower(gd, former)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, current)).isEqualTo(4);
        former.setAttacking(true);
        current.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(current)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(former))));
    }

    @Test
    @DisplayName("A later Dress Down removes granted artifact landwalk but preserves the power bonus")
    void laterAbilityRemovalAllowsBlocking() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent attacker = addCreatureReady(player1, new MyrScrapling());
        harness.addToBattlefield(player2, new GoldmireBridge());
        Permanent blocker = addCreatureReady(player2, new MyrScrapling());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gloves.getAttachedTo()).isEqualTo(attacker.getId());

        harness.setHand(player1, List.of(new DressDown()));
        harness.setLibrary(player1, List.of(new MyrScrapling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dress Down");

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        attacker.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
