package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.DragonsClaw;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EntanglingVines.class, RuneclawBear.class, DragonsClaw.class})
class EntanglingVinesTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a tapped creature with Entangling Vines")
    void canTargetTappedCreature() {
        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());
        bearsPerm.tap();

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an untapped creature with Entangling Vines")
    void cannotTargetUntappedCreature() {
        Permanent tappedBears = addCreatureReady(player2, new RuneclawBear());
        tappedBears.tap();

        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Entangling Vines")
    void cannotTargetNonCreature() {
        Permanent tappedBears = addCreatureReady(player2, new RuneclawBear());
        tappedBears.tap();

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        artifact.tap();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Casting Entangling Vines puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());
        bearsPerm.tap();

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Entangling Vines");
    }

    @Test
    @DisplayName("Resolving Entangling Vines attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());
        bearsPerm.tap();

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Entangling Vines")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Tapped creature with Entangling Vines does not untap during controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());
        bearsPerm.tap();

        Permanent vinesPerm = harness.addToBattlefieldAndReturn(player1, new EntanglingVines());
        vinesPerm.setAttachedTo(bearsPerm.getId());

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other permanents owned by the same player still untap normally")
    void otherPermanentsStillUntap() {
        Permanent enchantedBears = addCreatureReady(player2, new RuneclawBear());
        enchantedBears.tap();

        Permanent freeBears = addCreatureReady(player2, new RuneclawBear());
        freeBears.tap();

        Permanent vinesPerm = harness.addToBattlefieldAndReturn(player1, new EntanglingVines());
        vinesPerm.setAttachedTo(enchantedBears.getId());

        advanceToNextTurn(player1);

        assertThat(enchantedBears.isTapped()).isTrue();
        assertThat(freeBears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature can untap again after Entangling Vines is removed")
    void creatureUntapsAfterVinesRemoved() {
        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());
        bearsPerm.tap();

        Permanent vinesPerm = harness.addToBattlefieldAndReturn(player1, new EntanglingVines());
        vinesPerm.setAttachedTo(bearsPerm.getId());

        gd.playerBattlefields.get(player1.getId()).remove(vinesPerm);

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Full integration: cast Entangling Vines on tapped creature, advance turn, creature stays tapped")
    void fullIntegrationCastAndPreventUntap() {
        Permanent bearsPerm = addCreatureReady(player2, new RuneclawBear());
        bearsPerm.tap();

        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Entangling Vines")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));

        advanceToNextTurn(player1);

        assertThat(bearsPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped target is illegal when Entangling Vines resolves")
    void targetUntappingBeforeResolutionPreventsAttachment() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        creature.tap();
        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, creature.getId());

        creature.untap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Entangling Vines");
        harness.assertInGraveyard(player1, "Entangling Vines");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entangling Vines goes to the graveyard when its host becomes untapped")
    void untappedHostMakesAttachmentIllegal() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        creature.tap();
        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Entangling Vines");

        creature.untap();
        harness.runStateBasedActions();

        assertThat(creature.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Entangling Vines");
        harness.assertInGraveyard(player1, "Entangling Vines");
    }

    @Test
    @DisplayName("Entangling Vines can enchant its controller's tapped creature")
    void canEnchantOwnCreatureAndPreventItsUntap() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        creature.tap();
        harness.setHand(player1, List.of(new EntanglingVines()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Entangling Vines").getAttachedTo())
                .isEqualTo(creature.getId());
        harness.performUntapStep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
