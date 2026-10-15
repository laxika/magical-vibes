package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.d.DreadReturn;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InquisitorEisenhorn.class, DreadReturn.class, Brainstorm.class, Forest.class})
class InquisitorEisenhornTest extends BaseCardTest {

    @Test
    @DisplayName("May reveal the first instant or sorcery drawn on any turn to create Cherubael")
    void revealsInstantOrSorceryAndCreatesCherubael() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new DreadReturn(), new Forest()));
        gd.activePlayerId = player2.getId();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cherubael")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create Cherubael when the revealed card is not an instant or sorcery")
    void doesNotCreateCherubaelForOtherCardTypes() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Cherubael")).isEmpty();
    }

    @Test
    @DisplayName("Investigates once for each combat damage dealt to a player")
    void investigatesForCombatDamageAmount() {
        Permanent inquisitor = addCreatureReady(player1, new InquisitorEisenhorn());
        inquisitor.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("Accepting the reveal publicly identifies the drawn card")
    void acceptedRevealIsPublic() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new DreadReturn(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cherubael")).hasSize(1);
        assertThat(gd.gameLog).anySatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals", "Dread Return"));
    }

    @Test
    @DisplayName("Declining the first draw reveal creates no token and does not enable a later draw")
    void decliningDoesNotEnableSecondDraw() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new DreadReturn(), new Brainstorm(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, false);
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Cherubael")).isEmpty();
        assertThat(gd.gameLog).noneSatisfy(entry ->
                assertThat(entry.plainText()).contains("reveals", "Dread Return"));
    }

    @Test
    @DisplayName("An instant drawn first also creates Cherubael")
    void firstInstantCreatesCherubael() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new Brainstorm(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cherubael")).hasSize(1);
    }

    @Test
    @DisplayName("Drawing an opponent's first card does not trigger Eisenhorn")
    void opponentsDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player2, List.of(new Brainstorm(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Cherubael")).isEmpty();
        assertThat(findPermanents(player2, "Cherubael")).isEmpty();
    }

    @Test
    @DisplayName("Entering after the first draw does not allow the second card to be revealed")
    void enteringAfterFirstDrawDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new Forest(), new Brainstorm()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.addToBattlefield(player1, new InquisitorEisenhorn());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Cherubael")).isEmpty();
    }

    @Test
    @DisplayName("The first reveal choice interrupts a multiple-card draw before the next card")
    void revealChoicePrecedesSecondDraw() {
        harness.addToBattlefield(player1, new InquisitorEisenhorn());
        harness.setLibrary(player1, List.of(new Brainstorm(), new DreadReturn(), new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 2));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Cherubael")).hasSize(1);
    }

    @Test
    @DisplayName("A Clue from combat damage can be sacrificed for two mana to draw")
    void combatClueDrawsCard() {
        Permanent inquisitor = addCreatureReady(player1, new InquisitorEisenhorn());
        inquisitor.setAttacking(true);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        int handSize = gd.playerHands.get(player1.getId()).size();
        Permanent clue = findPermanent(player1, "Clue");
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, clueIndex, null, null);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(findPermanents(player1, "Cherubael")).isEmpty();
    }
}
