package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HollowSpecter.class, GrizzlyBears.class, HillGiant.class})
class HollowSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets the controller pay X and discard one of X revealed cards")
    void combatDamagePaysXAndDiscardsFromRevealedCards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant(), new GrizzlyBears())));
        Permanent specter = addReadyCreature(player1, new HollowSpecter());
        specter.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.resolveCombatDamage();
        harness.passBothPriorities();

        PendingInteraction.XValueChoice xChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(xChoice).isNotNull();
        assertThat(xChoice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        PendingInteraction.RevealCardsDiscardChoice reveal =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(reveal).isNotNull();
        assertThat(reveal.revealStage()).isTrue();
        assertThat(reveal.decidingPlayerId()).isEqualTo(player2.getId());
        assertThat(reveal.remainingCount()).isEqualTo(2);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);

        PendingInteraction.RevealCardsDiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.RevealCardsDiscardChoice.class);
        assertThat(discard).isNotNull();
        assertThat(discard.revealStage()).isFalse();
        assertThat(discard.decidingPlayerId()).isEqualTo(player1.getId());
        assertThat(discard.revealedCardIds()).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Choosing X=0 declines the ability and does not spend mana")
    void choosingZeroDeclines() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        Permanent specter = addReadyCreature(player1, new HollowSpecter());
        specter.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
