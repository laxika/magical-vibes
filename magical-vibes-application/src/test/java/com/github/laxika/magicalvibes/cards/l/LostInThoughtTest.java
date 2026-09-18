package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({LostInThought.class, CabalTrainee.class, SuntailHawk.class, KrosanVerge.class})
class LostInThoughtTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack or activate abilities")
    void enchantedCreatureIsLocked() {
        Permanent creature = addCreatureReady(player1, new CabalTrainee());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        addCreatureReady(player2, new SuntailHawk());
        Permanent blocker = addCreatureReady(player1, new CabalTrainee());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(blocker.getId());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(
                        gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolving Lost in Thought attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new LostInThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Lost in Thought");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Lost in Thought can target only a creature")
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        harness.setHand(player1, List.of(new LostInThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The enchanted creature's controller may exile three graveyard cards to ignore the Aura")
    void exilingThreeCardsIgnoresTheAuraUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new CabalTrainee());
        Permanent target = addCreatureReady(player1, new SuntailHawk());
        harness.setGraveyard(player1, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));

        Permanent aura = addOpponentAuraAtIndexTwo(creature);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cabal Trainee");
    }

    @Test
    @DisplayName("The Aura's controller cannot activate the bypass ability")
    void auraControllerCannotActivateBypass() {
        Permanent creature = addCreatureReady(player1, new CabalTrainee());

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
        Permanent creature = addCreatureReady(player1, new CabalTrainee());
        Permanent target = addCreatureReady(player1, new SuntailHawk());
        harness.setGraveyard(player1, List.of(new SuntailHawk(), new SuntailHawk(), new SuntailHawk()));

        Permanent aura = addOpponentAuraAtIndexTwo(creature);

        harness.activateAbility(player1, 2, 0, null, null);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("The bypass ability cannot be activated with fewer than three graveyard cards")
    void cannotBypassWithFewerThanThreeCards() {
        Permanent creature = addCreatureReady(player1, new CabalTrainee());
        harness.setGraveyard(player1, List.of(new SuntailHawk(), new SuntailHawk()));
        Permanent aura = addOpponentAuraAtIndexTwo(creature);

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The bypass ability exiles exactly three cards when more are available")
    void exilingExactlyThreeCardsWhenMoreAreAvailable() {
        Permanent creature = addCreatureReady(player1, new CabalTrainee());
        List<Card> graveyard = List.of(
                new SuntailHawk(), new SuntailHawk(), new SuntailHawk(), new SuntailHawk());
        harness.setGraveyard(player1, graveyard);
        Permanent aura = addOpponentAuraAtIndexTwo(creature);

        harness.activateAbility(player1, 2, 0, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);

        harness.handleMultipleCardsChosen(player1,
                List.of(graveyard.get(0).getId(), graveyard.get(1).getId(), graveyard.get(2).getId()));
        harness.passBothPriorities();

        assertThat(aura.isAuraEffectsIgnoredThisTurn()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(graveyard.get(0), graveyard.get(1), graveyard.get(2));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(3));
    }

    private Permanent addOpponentAuraAtIndexTwo(Permanent enchantedCreature) {
        addCreatureReady(player2, new SuntailHawk());
        addCreatureReady(player2, new SuntailHawk());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new LostInThought());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
