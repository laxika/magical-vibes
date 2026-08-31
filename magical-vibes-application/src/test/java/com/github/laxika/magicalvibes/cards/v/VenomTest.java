package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.cards.d.DarkSphere;
import com.github.laxika.magicalvibes.cards.s.ScarwoodGoblins;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.cards.w.WormwoodTreefolk;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Venom.class, Squire.class, ScarwoodGoblins.class, WormwoodTreefolk.class,
        CarnivorousPlant.class, DarkSphere.class})
class VenomTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature blocks a non-Wall attacker, that attacker is scheduled for end-of-combat destruction")
    void enchantedCreatureBlocksNonWall_schedulesDestruction() {
        Permanent blocker = addCreatureReady(player2, new Squire());
        Permanent venom = addVenomAttachedTo(player2, blocker);

        Permanent attacker = addCreatureReady(player1, new ScarwoodGoblins());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // The block trigger references the blocked attacker (the "other creature"), sourced from Venom
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Venom")
                        && se.getTargetId().equals(attacker.getId())
                        && se.getSourcePermanentId().equals(venom.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("When enchanted creature blocks a Wall, Venom does not trigger")
    void enchantedCreatureBlocksWall_doesNotTrigger() {
        Permanent blocker = addCreatureReady(player2, new Squire());
        addVenomAttachedTo(player2, blocker);

        Permanent attacker = addCreatureReady(player1, new ScarwoodGoblins());
        TestCards.mutableCard(attacker).setSubtypes(List.of(CardSubtype.WALL));
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Venom"));
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("When enchanted creature becomes blocked by a non-Wall creature, that blocker is scheduled for end-of-combat destruction")
    void enchantedCreatureBecomesBlockedByNonWall_schedulesDestruction() {
        Permanent attacker = addCreatureReady(player1, new Squire());
        Permanent venom = addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ScarwoodGoblins());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Venom")
                        && se.getTargetId().equals(blocker.getId())
                        && se.getSourcePermanentId().equals(venom.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("A non-Wall blocker survives combat damage but is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new Squire());
        addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addCreatureReady(player2, new WormwoodTreefolk());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the trigger, then advance through end of combat
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wormwood Treefolk");
        harness.assertInGraveyard(player2, "Wormwood Treefolk");
    }

    @Test
    @DisplayName("When enchanted creature becomes blocked by a Wall, Venom does not trigger")
    void becomesBlockedByWall_doesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new Squire());
        addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        addCreatureReady(player2, new CarnivorousPlant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Venom"));
        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("No trigger when Venom is not attached to any creature")
    void noTriggerWhenNotAttached() {
        addCreatureReady(player2, new Squire());
        addVenom(player2); // on battlefield but not attached

        Permanent attacker = addCreatureReady(player1, new ScarwoodGoblins());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        long venomTriggers = gd.stack.stream()
                .filter(se -> se.getCard().getName().equals("Venom"))
                .count();
        assertThat(venomTriggers).isZero();
    }

    @Test
    @DisplayName("Each non-Wall blocker creates its own Venom trigger")
    void eachNonWallBlockerCreatesItsOwnTrigger() {
        Permanent attacker = addCreatureReady(player1, new WormwoodTreefolk());
        addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new ScarwoodGoblins());
        Permanent secondBlocker = addCreatureReady(player2, new Squire());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack.stream()
                .filter(se -> se.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(se -> se.getCard().getName().equals("Venom")))
                .hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .extracting(DelayedPermanentAction::permanentId)
                .containsExactlyInAnyOrder(firstBlocker.getId(), secondBlocker.getId());
    }

    @Test
    @DisplayName("The non-Wall condition is evaluated when the trigger event occurs")
    void nonWallConditionIsNotRecheckedAfterTriggering() {
        Permanent attacker = addCreatureReady(player1, new Squire());
        addVenomAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ScarwoodGoblins());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        TestCards.mutableCard(blocker).setSubtypes(List.of(CardSubtype.WALL));

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Venom can enchant a creature")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new Squire());
        harness.setHand(player1, List.of(new Venom()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Venom").getAttachedTo())
                .isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Venom cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarkSphere());
        harness.setHand(player1, List.of(new Venom()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addVenom(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Venom());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addVenomAttachedTo(Player player, Permanent creature) {
        Permanent perm = addVenom(player);
        perm.setAttachedTo(creature.getId());
        return perm;
    }

}
