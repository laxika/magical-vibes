package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FrogtosserBanneret;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloakAndDaggerTest extends BaseCardTest {

    // ===== Static: +2/+0 and shroud =====

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has shroud")
    void equippedCreatureHasShroud() {
        Permanent creature = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creature loses +2/+0 and shroud when Cloak is removed")
    void creatureLosesBonusesWhenCloakRemoved() {
        Permanent creature = addReadyCreature(player1);
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    // ===== Equip {3} =====

    @Test
    @DisplayName("Resolving equip attaches Cloak to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Trigger: Rogue creature enters =====

    @Test
    @DisplayName("Accepting the may attaches Cloak to the Rogue that entered")
    void attachesToEnteringRogueOnAccept() {
        Permanent cloak = addCloakReady(player1);

        harness.setHand(player1, List.of(new FrogtosserBanneret()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → Cloak triggers, may-ability on stack
        harness.passBothPriorities(); // resolve may-ability → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent rogue = rogueOnBattlefield(player1);
        assertThat(cloak.getAttachedTo()).isEqualTo(rogue.getId());
        // Frogtosser Banneret is a 1/1; the Cloak's +2/+0 makes it a 3/1.
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the may leaves Cloak unattached")
    void staysUnattachedOnDecline() {
        Permanent cloak = addCloakReady(player1);

        harness.setHand(player1, List.of(new FrogtosserBanneret()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(cloak.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for a non-Rogue creature entering")
    void doesNotTriggerForNonRogue() {
        addCloakReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell — no trigger for a Bear

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can attach to a Rogue an opponent controls")
    void attachesToOpponentRogue() {
        Permanent cloak = addCloakReady(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FrogtosserBanneret()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        // The Cloak's controller (player1) makes the "you may" choice.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent rogue = rogueOnBattlefield(player2);
        assertThat(cloak.getAttachedTo()).isEqualTo(rogue.getId());
    }

    // ===== Helpers =====

    private Permanent addCloakReady(Player player) {
        Permanent perm = new Permanent(new CloakAndDagger());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent rogueOnBattlefield(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Frogtosser Banneret"))
                .findFirst().orElseThrow();
    }
}
