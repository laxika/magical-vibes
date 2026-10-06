package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HedgeTroll;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiptidePilferer.class, HedgeTroll.class})
class RiptidePilfererTest extends BaseCardTest {

    @Test
    void combatDamageMakesDamagedPlayerDiscard() {
        addAttackingPilferer(player1);
        harness.setHand(player2, List.of(new HedgeTroll()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void blockedCombatDamageDoesNotTriggerDiscard() {
        addAttackingPilferer(player1);
        Permanent blocker = addCreatureReady(player2, new HedgeTroll());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new HedgeTroll()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForBlue() {
        harness.setHand(player1, List.of(new RiptidePilferer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent pilferer = findPermanent(player1, "Riptide Pilferer");
        assertThat(pilferer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pilferer));
        harness.passBothPriorities();

        assertThat(pilferer.isFaceDown()).isFalse();
    }

    @Test
    void damagedPlayerChoosesExactlyOneCardAndControllerKeepsTheirHand() {
        addAttackingPilferer(player1);
        HedgeTroll keptCard = new HedgeTroll();
        RiptidePilferer discardedCard = new RiptidePilferer();
        HedgeTroll controllerCard = new HedgeTroll();
        harness.setHand(player2, List.of(keptCard, discardedCard));
        harness.setHand(player1, List.of(controllerCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyHandDoesNotRequireDiscardChoice() {
        addAttackingPilferer(player1);
        harness.setHand(player2, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void faceDownCombatDamageDoesNotTriggerDiscard() {
        Permanent pilferer = addAttackingPilferer(player1);
        pilferer.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        HedgeTroll handCard = new HedgeTroll();
        harness.setHand(player2, List.of(handCard));

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(handCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void turningFaceUpBeforeCombatDamageRestoresDiscardAbility() {
        Permanent pilferer = addAttackingPilferer(player1);
        pilferer.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        HedgeTroll handCard = new HedgeTroll();
        harness.setHand(player2, List.of(handCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, 0);
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(handCard);
    }

    @Test
    void discardTriggerResolvesAfterPilfererLeavesBattlefield() {
        Permanent pilferer = addAttackingPilferer(player1);
        HedgeTroll handCard = new HedgeTroll();
        harness.setHand(player2, List.of(handCard));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(pilferer);
        gd.playerGraveyards.get(player1.getId()).add(pilferer.getCard());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(handCard);
    }

    private Permanent addAttackingPilferer(Player player) {
        Permanent pilferer = addCreatureReady(player, new RiptidePilferer());
        pilferer.setAttacking(true);
        return pilferer;
    }
}
