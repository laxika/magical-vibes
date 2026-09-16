package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.h.HarvesterDruid;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostInThought.class, HarvesterDruid.class, SuntailHawk.class})
class LostInThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack or activate abilities")
    void enchantedCreatureIsLocked() {
        Permanent creature = addCreatureReady(player1, new HarvesterDruid());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new SuntailHawk());
        attacker.setAttacking(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new LostInThought());
        Permanent blocker = addCreatureReady(player2, new HarvesterDruid());
        aura.setAttachedTo(blocker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("The enchanted creature's controller may exile three graveyard cards to ignore the Aura")
    void exilingThreeCardsIgnoresTheAuraUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new HarvesterDruid());
        harness.setGraveyard(player1, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));

        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The bypass ability cannot be activated without three cards in the graveyard")
    void bypassRequiresThreeGraveyardCards() {
        Permanent creature = addCreatureReady(player1, new HarvesterDruid());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(creature.getId());
        harness.setGraveyard(player1, List.of(new SuntailHawk(), new SuntailHawk()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough matching cards in graveyard");
        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Aura's controller cannot activate the bypass ability")
    void auraControllerCannotActivateBypass() {
        Permanent creature = addCreatureReady(player1, new HarvesterDruid());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(creature.getId());
        harness.setGraveyard(player2, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted permanent's controller");
    }

    @Test
    @DisplayName("The bypass ability wears off at end of turn")
    void bypassWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new HarvesterDruid());
        harness.setGraveyard(player1, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));

        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 2, 0, null, null);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }
}
