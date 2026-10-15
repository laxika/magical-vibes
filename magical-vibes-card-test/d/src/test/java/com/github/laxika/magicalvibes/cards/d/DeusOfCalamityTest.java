package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SoulsFire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeusOfCalamity.class, Forest.class, Mountain.class, GrizzlyBears.class, SoulsFire.class})
class DeusOfCalamityTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing 6 to an opponent prompts to destroy a land that player controls")
    void promptsToDestroyLand() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mountain.getId());
    }

    @Test
    @DisplayName("The chosen land is destroyed and the game advances")
    void destroysChosenLand() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();
        harness.handlePermanentChosen(player1, mountain.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the damaged player's lands are valid targets (not own lands, not creatures)")
    void onlyDamagedPlayersLands() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(enemyLand.getId())
                .doesNotContain(ownLand.getId())
                .doesNotContain(enemyCreature.getId());
    }

    @Test
    @DisplayName("No target choice when the damaged player controls no lands")
    void noTargetChoiceWithoutLands() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No trigger when fewer than 6 damage is dealt to the opponent")
    void noTriggerBelowThreshold() {
        DeusOfCalamity card = new DeusOfCalamity();
        card.setPower(5);
        Permanent deus = addCreatureReady(player1, card);
        deus.setAttacking(true);
        harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("More than six combat damage also triggers land destruction")
    void triggersAboveThreshold() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setPowerModifier(1);
        deus.setAttacking(true);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();

        harness.assertLife(player2, 13);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Damage assigned to a blocker does not count toward the six damage to the opponent")
    void trampleBelowThresholdDoesNotTrigger() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.getBlockingTargets().add(0);
        blocker.getBlockingTargetIds().add(deus.getId());
        harness.addToBattlefield(player2, new Mountain());

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 4));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Six noncombat damage from Deus triggers land destruction")
    void noncombatDamageTriggers() {
        Permanent deus = harness.addToBattlefieldAndReturn(player1, new DeusOfCalamity());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(deus.getId(), player2.getId()));

        harness.assertLife(player2, 14);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Six damage to Deus's controller does not trigger land destruction")
    void damageToControllerDoesNotTrigger() {
        Permanent deus = harness.addToBattlefieldAndReturn(player1, new DeusOfCalamity());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(deus.getId(), player1.getId()));

        harness.assertLife(player1, 14);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A targeted land that changes controllers is no longer a legal target")
    void landChangingControllerMakesTargetIllegal() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();
        harness.handlePermanentChosen(player1, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerBattlefields.get(player1.getId()).add(land);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        harness.assertNotInGraveyard(player2, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The land destruction ability resolves after Deus leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent deus = addCreatureReady(player1, new DeusOfCalamity());
        deus.setAttacking(true);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        resolveCombat();
        harness.handlePermanentChosen(player1, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(deus);
        gd.playerGraveyards.get(player1.getId()).add(deus.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Mountain");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two separate three-damage events do not meet the six-damage threshold")
    void separateDamageEventsDoNotAccumulate() {
        Permanent deus = harness.addToBattlefieldAndReturn(player1, new DeusOfCalamity());
        deus.setPowerModifier(-3);
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new SoulsFire(), new SoulsFire()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveInstant(player1, 0, List.of(deus.getId(), player2.getId()));
        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.castAndResolveInstant(player1, 0, List.of(deus.getId(), player2.getId()));

        harness.assertLife(player2, 14);
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
