package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlazingTorch;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KondasHatamoto;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GodoBanditWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the enter-the-battlefield trigger puts an Equipment onto the battlefield")
    void entersOffersEquipmentSearchToBattlefield() {
        castGodo();
        stockLibrary();

        harness.passBothPriorities(); // resolve the creature spell -> ETB trigger on the stack
        harness.passBothPriorities(); // resolve the trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).hasSize(1); // only the Equipment, not the creature or land
        assertThat(offered.getFirst().getName()).isEqualTo("Blazing Torch");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Blazing Torch");
    }

    @Test
    @DisplayName("Declining the enter-the-battlefield trigger skips the search")
    void decliningSkipsTheSearch() {
        castGodo();
        stockLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().getName().equals("Blazing Torch"));
    }

    @Test
    @DisplayName("Attacking untaps Godo and Samurai you control and grants an additional combat phase")
    void attackUntapsGodoAndSamuraiAndGrantsExtraCombat() {
        Permanent godo = addCreatureReady(player1, new GodoBanditWarlord());
        Permanent samurai = addCreatureReady(player1, new KondasHatamoto());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.tap();

        declareAttackers(player1, List.of(0, 1), 1);
        assertThat(godo.isTapped()).isTrue();
        assertThat(samurai.isTapped()).isTrue();

        harness.passBothPriorities(); // resolve the triggers

        assertThat(godo.isTapped()).isFalse();
        assertThat(samurai.isTapped()).isFalse();
        assertThat(bear.isTapped()).isTrue(); // non-Samurai, non-Godo creatures stay tapped

        // The additional combat phase followed directly, with no postcombat main phase between.
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
        assertThat(gd.combatPhasesThisTurn).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking a second time in the same turn does not trigger again")
    void secondAttackSameTurnDoesNotTrigger() {
        addCreatureReady(player1, new GodoBanditWarlord());

        declareAttackers(player1, List.of(0), 1);
        harness.passBothPriorities();

        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Godo, Bandit Warlord"));
        assertThat(gd.additionalCombatPhasesOnly).isEqualTo(0);
    }

    private void castGodo() {
        harness.setHand(player1, List.of(new GodoBanditWarlord()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
    }

    private void stockLibrary() {
        GameData gameData = harness.getGameData();
        List<Card> deck = gameData.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new BlazingTorch(), new GrizzlyBears()));
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, int combatPhaseNumber) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        gd.combatPhasesThisTurn = combatPhaseNumber;
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
