package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PryingBlade;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HammerOfRuin.class, GrizzlyBears.class, PryingBlade.class, Spellbook.class})
class HammerOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Hammer of Ruin destroys an Equipment controlled by the damaged player")
    void destroysDamagedPlayersEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PryingBlade());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Prying Blade");
        harness.assertInGraveyard(player2, "Prying Blade");
    }

    @Test
    @DisplayName("Only the damaged player's Equipment can be chosen")
    void onlyDamagedPlayersEquipmentIsValid() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new PryingBlade());
        Permanent enemyEquipment = harness.addToBattlefieldAndReturn(player2, new PryingBlade());
        Permanent enemyArtifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        resolveCombat();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyEquipment.getId())
                .doesNotContain(ownEquipment.getId(), enemyArtifact.getId());
    }

    @Test
    @DisplayName("Declining the may ability leaves the Equipment on the battlefield")
    void decliningDestroyingEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new PryingBlade());

        resolveCombat();
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Prying Blade");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No trigger occurs when the equipped creature deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.addToBattlefield(player2, new PryingBlade());

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private Permanent addHammerReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new HammerOfRuin());
    }
    @Test
    void equipAttachesToControlledCreature() {
        Permanent hammer = addHammerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void removingHammerRemovesPowerBonus() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = addHammerReady(player1);
        hammer.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(hammer);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }
}
