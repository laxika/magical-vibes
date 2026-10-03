package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CastingOfBones.class, Contagion.class, StormCrow.class})
class CastingOfBonesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting the Aura attaches it without drawing, and its trigger survives the Aura going to the graveyard")
    void castAuraAndResolveDeathTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new StormCrow());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CastingOfBones()));
        harness.setLibrary(player1, List.of(new StormCrow(), new StormCrow(), new StormCrow()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> creature.getId().equals(p.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        destroyWithContagion(player2, creature);

        harness.assertInGraveyard(player1, "Casting of Bones");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A later death trigger cannot discard cards drawn by an earlier trigger")
    void separateTriggersKeepTheirDrawnCardsSeparate() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StormCrow(), new StormCrow(), new StormCrow(),
                new StormCrow(), new StormCrow(), new StormCrow()));
        Permanent first = addCreatureWithAura(player1, player1);
        destroyWithContagion(player2, first);
        harness.handleCardChosen(player1, 0);
        List<Card> earlierCards = List.copyOf(gd.playerHands.get(player1.getId()));

        Permanent second = addCreatureWithAura(player1, player1);
        destroyWithContagion(player2, second);

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2, 3, 4);
        harness.handleCardChosen(player1, 4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).containsAll(earlierCards);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("When the enchanted creature dies, the Aura's controller draws three cards then discards one")
    void deathTriggerDrawsThreeAndDiscardsOne() {
        Permanent creature = addCreatureWithAura(player1, player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StormCrow(), new StormCrow(), new StormCrow()));

        destroyWithContagion(player2, creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Storm Crow");
    }

    @Test
    @DisplayName("The trigger only allows discarding one of the three cards it drew")
    void discardChoiceIsLimitedToCardsDrawnByTrigger() {
        Permanent creature = addCreatureWithAura(player1, player1);
        Card preexistingCard = new StormCrow();
        harness.setHand(player1, List.of(preexistingCard));
        harness.setLibrary(player1, List.of(new StormCrow(), new StormCrow(), new StormCrow()));
        destroyWithContagion(player2, creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("The Aura's controller draws even when it enchants an opponent's creature")
    void auraControllerDrawsWhenEnchantingOpponentCreature() {
        Permanent creature = addCreatureWithAura(player2, player1);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new StormCrow(), new StormCrow(), new StormCrow()));

        destroyWithContagion(player1, creature);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when a creature other than the enchanted one dies")
    void noTriggerWhenDifferentCreatureDies() {
        Permanent enchanted = addCreatureWithAura(player1, player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StormCrow(), new StormCrow(), new StormCrow()));

        Permanent other = harness.addToBattlefieldAndReturn(player2, new StormCrow());

        destroyWithContagion(player1, other);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(enchanted.getId()));
    }

    private void destroyWithContagion(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Contagion()));
        harness.addMana(caster, ManaColor.BLACK, 5);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities(); // resolve Contagion - creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve the death trigger
    }

    /**
     * Places a Storm Crow on the creature controller's battlefield and attaches a
     * Casting of Bones controlled by the aura controller.
     *
     * @return the Storm Crow permanent
     */
    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new StormCrow());

        Card auraCard = new CastingOfBones();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);

        return creature;
    }
}
