package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SilvarDevourerOfTheFree;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrynnChampionOfFreedom.class, SilvarDevourerOfTheFree.class})
class TrynnChampionOfFreedomTest extends BaseCardTest {

    @Test
    @DisplayName("Target player may search for Silvar and put it into their hand")
    void targetPlayerMaySearchForPartner() {
        Card partner = new SilvarDevourerOfTheFree();
        TrynnChampionOfFreedom decoy = new TrynnChampionOfFreedom();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(decoy, partner));
        harness.setHand(player1, List.of(new TrynnChampionOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player2.getId());
        assertThat(search.params().targetPlayerId()).isEqualTo(player2.getId());
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Silvar, Devourer of the Free");

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Silvar, Devourer of the Free");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
    }

    @Test
    @DisplayName("Creates a Human Soldier token at your end step after attacking")
    void createsSoldierTokenAfterAttacking() {
        harness.addToBattlefield(player1, new TrynnChampionOfFreedom());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        advanceToEndStep();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
                });
    }

    @Test
    @DisplayName("Does not create a token at your end step if you did not attack")
    void doesNotCreateSoldierTokenWithoutAttacking() {
        harness.addToBattlefield(player1, new TrynnChampionOfFreedom());

        advanceToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }

    @Test
    void targetPlayerCanDeclinePartnerSearch() {
        SilvarDevourerOfTheFree partner = new SilvarDevourerOfTheFree();
        harness.setLibrary(player2, List.of(partner));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TrynnChampionOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(partner);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void controllerCanSearchForPartnerAndFailToFind() {
        SilvarDevourerOfTheFree partner = new SilvarDevourerOfTheFree();
        harness.setLibrary(player1, List.of(partner));
        harness.setHand(player1, List.of(new TrynnChampionOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(partner);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void searchWithoutPartnerLeavesOtherCardsInLibrary() {
        TrynnChampionOfFreedom decoy = new TrynnChampionOfFreedom();
        harness.setLibrary(player2, List.of(decoy));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TrynnChampionOfFreedom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(decoy);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new TrynnChampionOfFreedom());
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void createsTokenWhenAnotherCreatureAttackedBeforeTrynnEntered() {
        addCreatureReady(player1, new SilvarDevourerOfTheFree());
        declareAttackers(List.of(0));
        resolveCombat();
        harness.addToBattlefield(player1, new TrynnChampionOfFreedom());

        advanceToEndStep();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }
}
