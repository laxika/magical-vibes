package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MartyrOfFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals blue cards, counters an unpaid spell, and sacrifices itself")
    void countersWhenOpponentCannotPay() {
        Counterspell firstBlueCard = new Counterspell();
        Unsummon secondBlueCard = new Unsummon();
        harness.setHand(player1, List.of(firstBlueCard, secondBlueCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfFrost());

        harness.forceActivePlayer(player2);
        RagingGoblin goblin = new RagingGoblin();
        harness.setHand(player2, List.of(goblin));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, goblin.getId());

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBlueCard.getId(), secondBlueCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlueCard.getId(), secondBlueCard.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Raging Goblin");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstBlueCard, secondBlueCard);
    }

    @Test
    @DisplayName("Opponent can pay X and the targeted spell resolves")
    void leavesSpellUncounteredWhenOpponentPays() {
        Counterspell blueCard = new Counterspell();
        harness.setHand(player1, List.of(blueCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfFrost());

        harness.forceActivePlayer(player2);
        RagingGoblin goblin = new RagingGoblin();
        harness.setHand(player2, List.of(goblin));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, goblin.getId());
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Raging Goblin");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
    }

    @Test
    @DisplayName("Cannot activate without enough blue cards to reveal")
    void cannotRevealMoreBlueCardsThanAreInHand() {
        harness.setHand(player1, List.of(new RagingGoblin()));
        Permanent martyr = addCreatureReady(player1, new MartyrOfFrost());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
