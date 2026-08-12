package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExplorersScopeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the top land onto the battlefield tapped when accepted after an equipped creature attacks")
    void putsTopLandOntoBattlefieldTapped() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        declareScopeAttackers(player1, List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest") && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Leaves the matching top card on the library when declined")
    void declinedLeavesLandOnTop() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        declareScopeAttackers(player1, List.of(0));
        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Does not offer a choice for a nonland top card")
    void nonlandTopCardStaysOnTop() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, deckOf(new GrizzlyBears(), new Forest()));

        declareScopeAttackers(player1, List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger when the Scope is not attached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new GrizzlyBears());
        addScope(player1);
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        declareScopeAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Explorer's Scope"));
    }

    private Permanent addScope(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ExplorersScope());
    }

    private void resolveAttackTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private void declareScopeAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, Map.of());
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
