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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloakAndDagger.class, FrogtosserBanneret.class, GrizzlyBears.class})
class CloakAndDaggerTest extends BaseCardTest {


    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has shroud")
    void equippedCreatureHasShroud() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creature loses +2/+0 and shroud when Cloak is removed")
    void creatureLosesBonusesWhenCloakRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }


    @Test
    @DisplayName("Resolving equip attaches Cloak to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent cloak = addCloakReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(creature.getId());
    }


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

    @Test
    @DisplayName("The enter trigger can attach to a Rogue with shroud")
    void attachesToEnteringRogueWithShroud() {
        Permanent cloak = addCloakReady(player1);
        Permanent otherCloak = addCloakReady(player1);
        harness.setHand(player1, List.of(new FrogtosserBanneret()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent rogue = rogueOnBattlefield(player1);
        otherCloak.setAttachedTo(rogue.getId());
        assertThat(gqs.hasKeyword(gd, rogue, Keyword.SHROUD)).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cloak.getAttachedTo()).isEqualTo(rogue.getId());
        assertThat(otherCloak.getAttachedTo()).isEqualTo(rogue.getId());
        assertThat(gqs.getEffectivePower(gd, rogue)).isEqualTo(5);
    }

    @Test
    @DisplayName("Accepting the enter trigger moves Cloak from its previous creature")
    void triggerMovesEquipmentAndItsBonuses() {
        Permanent original = addCreatureReady(player1, new FrogtosserBanneret());
        Permanent cloak = addCloakReady(player1);
        cloak.setAttachedTo(original.getId());
        harness.setHand(player1, List.of(new FrogtosserBanneret()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent entering = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof FrogtosserBanneret && p != original)
                .findFirst().orElseThrow();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(cloak.getAttachedTo()).isEqualTo(entering.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, original, Keyword.SHROUD)).isFalse();
        assertThat(gqs.getEffectivePower(gd, entering)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, entering, Keyword.SHROUD)).isTrue();
    }

    private Permanent addCloakReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CloakAndDagger());
    }

    private Permanent rogueOnBattlefield(Player player) {
        return findPermanent(player, "Frogtosser Banneret");
    }
}
