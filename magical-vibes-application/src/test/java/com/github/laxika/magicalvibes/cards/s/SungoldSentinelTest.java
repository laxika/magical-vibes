package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SungoldSentinel.class, GrizzlyBears.class, RagingGoblin.class})
class SungoldSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Entering exiles up to one target card from a graveyard")
    void enteringExilesTargetCardFromAnyGraveyard() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new RagingGoblin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCard)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCard)));

        harness.setHand(player1, List.of(new SungoldSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(opponentCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(ownCard.getId());
    }

    @Test
    @DisplayName("Attacking exiles up to one target card from a graveyard")
    void attackingExilesTargetCardFromAnyGraveyard() {
        Permanent sentinel = new Permanent(new SungoldSentinel());
        sentinel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sentinel);
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId).contains(card.getId());
    }

    @Test
    @DisplayName("Coven grants chosen-color hexproof and evasion to Sungold Sentinel")
    void covenGrantsChosenColorHexproofAndEvasion() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sentinel), null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasHexproofFromColor(gd, sentinel, CardColor.RED)).isTrue();

        Permanent redBlocker = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        sentinel.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(redBlocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Coven cannot be activated without three different powers")
    void covenRequiresThreeDifferentPowers() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new SungoldSentinel());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(sentinel), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different powers");
    }
}
