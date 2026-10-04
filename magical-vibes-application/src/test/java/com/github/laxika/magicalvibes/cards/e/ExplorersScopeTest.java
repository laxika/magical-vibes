package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExplorersScope.class, Forest.class, GrizzlyBears.class})
class ExplorersScopeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the top land onto the battlefield tapped when accepted after an equipped creature attacks")
    void putsTopLandOntoBattlefieldTapped() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, deckOf(new Forest(), new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
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

        declareAttackers(player1, List.of(0));
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

        declareAttackers(player1, List.of(0));
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

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Explorer's Scope"));
    }

    @Test
    @DisplayName("The controller privately sees a nonland top card")
    void privatelyLooksAtNonland() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        declareAttackers(List.of(0));
        harness.clearMessages();
        resolveAttackTrigger();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_LIBRARY_TOP"))
                .anyMatch(message -> message.contains("Grizzly Bears"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_LIBRARY_TOP")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An empty library does not offer a choice or cause a loss")
    void emptyLibraryDoesNothing() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAttackTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Scope's controller uses their own library when an opponent controls the equipped creature")
    void usesEquipmentControllersLibrary() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        Card opponentTop = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentTop, new Forest()));

        declareAttackers(player2, List.of(0));
        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(opponentTop);
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("A queued attack trigger resolves after Scope leaves the battlefield")
    void triggerSurvivesScopeLeaving() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scope = addScope(player1);
        scope.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(scope);
        gd.playerGraveyards.get(player1.getId()).add(scope.getCard());
        resolveAttackTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Scope for one mana")
    void equipsCreature() {
        Permanent scope = addScope(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scope.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent scope = addScope(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scope.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent scope = addScope(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scope.isAttached()).isFalse();
    }

    private Permanent addScope(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ExplorersScope());
    }

    private void resolveAttackTrigger() {
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }

    private List<Card> deckOf(Card... cards) {
        return new ArrayList<>(List.of(cards));
    }
}
