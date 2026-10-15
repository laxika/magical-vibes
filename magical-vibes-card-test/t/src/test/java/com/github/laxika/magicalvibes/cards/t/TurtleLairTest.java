package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.m.Metalhead;
import com.github.laxika.magicalvibes.cards.m.MouserMarkIII;
import com.github.laxika.magicalvibes.cards.p.PrehistoricPet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurtleLair.class, Metalhead.class, MouserMarkIII.class, PrehistoricPet.class, Bitterblossom.class})
class TurtleLairTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void firstAbilityAddsColorlessMana() {
        Permanent lair = addReadyLair();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lair.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability adds one mana of a chosen color for Ninja or Turtle spells")
    void secondAbilityAddsNinjaOrTurtleSpellOnlyMana() {
        Permanent lair = addReadyLair();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(lair.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.BLUE)).isZero();
        assertThat(pool().getSubtypeSpellOnlyManaForColor(
                Set.of(CardSubtype.NINJA, CardSubtype.TURTLE), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ninja or Turtle spell-only mana can cast a matching spell")
    void restrictedManaCanCastMatchingSpell() {
        pool().addSubtypeSpellOnlyMana(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE), ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new Metalhead()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isZero();
    }

    @Test
    @DisplayName("Ninja or Turtle spell-only mana cannot cast another spell")
    void restrictedManaCannotCastOtherSpell() {
        pool().addSubtypeSpellOnlyMana(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE), ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new MouserMarkIII()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isEqualTo(1);
    }

    @Test
    @DisplayName("The third ability makes a target Turtle unable to be blocked this turn")
    void thirdAbilityMakesTargetUnblockable() {
        addReadyLair();
        Permanent turtle = addCreatureReady(player2, new Metalhead());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, turtle.getId());
        harness.passBothPriorities();

        assertThat(turtle.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The unblockable effect wears off at cleanup")
    void unblockableWearsOffAtCleanup() {
        addReadyLair();
        Permanent turtle = addCreatureReady(player1, new Metalhead());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, turtle.getId());
        harness.passBothPriorities();
        assertThat(turtle.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(turtle.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The third ability cannot target a creature without the Ninja or Turtle subtype")
    void thirdAbilityRejectsOtherCreature() {
        addReadyLair();
        Permanent bears = addCreatureReady(player2, new MouserMarkIII());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ninja or Turtle");
    }

    @Test
    void chosenManaCastsNinjaWithoutTurtleSubtype() {
        addReadyLair();
        harness.setHand(player1, List.of(new PrehistoricPet()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");
        assertThat(gd.stack).isEmpty();
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isZero();
    }

    @Test
    void chosenManaCastsTurtleWithoutNinjaSubtype() {
        addReadyLair();
        harness.setHand(player1, List.of(new Metalhead()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isZero();
        assertThat(pool().get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void chosenManaPaysGenericPartOfTurtleSpell() {
        addReadyLair();
        harness.setHand(player1, List.of(new Metalhead()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isZero();
        assertThat(pool().get(ManaColor.BLUE)).isZero();
        assertThat(pool().get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void restrictedManaCannotPayNinjaActivatedAbility() {
        addReadyLair();
        Permanent pet = addCreatureReady(player1, new PrehistoricPet());
        Permanent turtle = addCreatureReady(player1, new Metalhead());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, turtle.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(pet.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(pool().getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.NINJA, CardSubtype.TURTLE))).isEqualTo(1);
    }

    @Test
    void ninjaTargetUsesStackAndPaysThreeMana() {
        Permanent lair = addReadyLair();
        Permanent ninja = addCreatureReady(player1, new PrehistoricPet());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 2, null, ninja.getId());

        assertThat(lair.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(ninja.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        assertThat(ninja.isCantBeBlocked()).isTrue();
    }

    @Test
    void unblockableTargetCannotBeBlocked() {
        addReadyLair();
        addCreatureReady(player1, new Metalhead());
        addCreatureReady(player2, new MouserMarkIII());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent turtle = findPermanent(player1, "Metalhead");
        harness.activateAbility(player1, 0, 2, null, turtle.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(1));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void grantingUnblockableDoesNotUndoExistingBlock() {
        addReadyLair();
        Permanent turtle = addCreatureReady(player1, new Metalhead());
        Permanent blocker = addCreatureReady(player2, new MouserMarkIII());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        declareAttackersAndPrepareBlockers(List.of(1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.activateAbility(player1, 0, 2, null, turtle.getId());
            harness.passBothPriorities();
        });

        assertThat(turtle.isCantBeBlocked()).isTrue();
        assertThat(blocker.getBlockingTargetIds()).containsExactly(turtle.getId());
        assertThat(gqs.isBlockedByAnyCreature(gd, turtle)).isTrue();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void thirdAbilityCannotActivateWithOnlyTwoMana() {
        Permanent lair = addReadyLair();
        Permanent turtle = addCreatureReady(player1, new Metalhead());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, turtle.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lair.isTapped()).isFalse();
        assertThat(turtle.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityWorksOnTurnLandEnters() {
        Permanent lair = harness.addToBattlefieldAndReturn(player1, new TurtleLair());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lair.isTapped()).isTrue();
        assertThat(pool().get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void thirdAbilityCanTargetNoncreatureKindredNinja() {
        addReadyLair();
        // Represents Bitterblossom after Artificial Evolution changes Faerie to Ninja.
        Card kindred = new Bitterblossom().createRuntimeCopy();
        kindred.setSubtypes(List.of(CardSubtype.NINJA));
        Permanent target = harness.addToBattlefieldAndReturn(player1, kindred);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    private Permanent addReadyLair() {
        return addCreatureReady(player1, new TurtleLair());
    }

    private ManaPool pool() {
        return gd.playerManaPools.get(player1.getId());
    }
}
