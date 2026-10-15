package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.t.TheWarDoctor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BecomeThePilot.class, FountainOfYouth.class, GrizzlyBears.class,
        JaceBeleren.class, TheWarDoctor.class})
class BecomeThePilotTest extends BaseCardTest {

    @Test
    @DisplayName("Become the Pilot takes control and gives the creature +2/+2")
    void takesControlAndBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Become the Pilot makes a creature unblockable when it attacks a non-owner")
    void cannotBeBlockedWhenAttackingNonOwner() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player1.getId());
        Permanent creature = addCreatureReady(player1, creatureCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BecomeThePilot());
        aura.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Become the Pilot allows blocking when the creature attacks its owner")
    void canBeBlockedWhenAttackingOwner() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = addCreatureReady(player1, creatureCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BecomeThePilot());
        aura.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Become the Pilot cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncommander creature");
    }

    @Test
    @DisplayName("Become the Pilot cannot enchant a commander creature")
    void cannotTargetCommanderCreature() {
        Permanent commander = addCreatureReady(player2, new TheWarDoctor());
        commander.setCommander(true);
        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, commander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncommander creature");
    }

    @Test
    @DisplayName("A legendary creature that is not a commander is a legal target")
    void canEnchantLegendaryNonCommander() {
        Permanent creature = addCreatureReady(player2, new TheWarDoctor());
        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(findPermanent(player1, "Become the Pilot").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Removing Become the Pilot ends its control and power/toughness effects")
    void removingAuraRestoresControlAndStats() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = addCreatureReady(player2, creatureCard);
        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Become the Pilot");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, aura);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The enchanted creature can be blocked while attacking a planeswalker its owner controls")
    void canBeBlockedWhenAttackingOwnerControlledPlaneswalker() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = addCreatureReady(player1, creatureCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BecomeThePilot());
        aura.setAttachedTo(creature.getId());
        JaceBeleren planeswalkerCard = new JaceBeleren();
        planeswalkerCard.setOwnerId(player1.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        creature.setAttacking(true);
        creature.setAttackTarget(planeswalker.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Owning the attacked planeswalker does not permit blocks if its controller is not the creature's owner")
    void cannotBeBlockedWhenAttackingPlaneswalkerOwnerDoesNotControl() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player1.getId());
        Permanent creature = addCreatureReady(player1, creatureCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BecomeThePilot());
        aura.setAttachedTo(creature.getId());
        JaceBeleren planeswalkerCard = new JaceBeleren();
        planeswalkerCard.setOwnerId(player1.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        creature.setAttacking(true);
        creature.setAttackTarget(planeswalker.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Become the Pilot goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player2.getId());
        Permanent creature = addCreatureReady(player2, creatureCard);
        harness.setHand(player1, List.of(new BecomeThePilot()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Become the Pilot");
        harness.assertInGraveyard(player1, "Become the Pilot");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Removing the Aura ends its blocking restriction")
    void removingAuraRestoresBlockability() {
        GrizzlyBears creatureCard = new GrizzlyBears();
        creatureCard.setOwnerId(player1.getId());
        Permanent creature = addCreatureReady(player1, creatureCard);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BecomeThePilot());
        aura.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        creature.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(bls.canBlockAttacker(gd, blocker, creature,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
