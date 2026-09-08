package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PryingBlade;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(equipment.getId()));

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
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
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
        harness.addToBattlefield(player2, new PryingBlade());

        resolveCombat();
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
        Permanent perm = new Permanent(new HammerOfRuin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
