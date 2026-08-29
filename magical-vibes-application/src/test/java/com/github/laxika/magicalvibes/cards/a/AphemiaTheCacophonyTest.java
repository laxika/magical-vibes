package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphemiaTheCacophony.class, OmenOfTheSea.class, GrizzlyBears.class})
class AphemiaTheCacophonyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling an enchantment card from the graveyard creates a Zombie")
    void exilesEnchantmentAndCreatesZombie() {
        Card creature = new GrizzlyBears();
        Card enchantment = new OmenOfTheSea();
        harness.setGraveyard(player1, List.of(creature, enchantment));

        triggerAphemia();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Zombie"));
    }

    @Test
    @DisplayName("The optional graveyard exile can be declined")
    void exileCanBeDeclined() {
        Card enchantment = new OmenOfTheSea();
        harness.setGraveyard(player1, List.of(enchantment));

        triggerAphemia();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Zombie"));
    }

    @Test
    @DisplayName("A non-enchantment card cannot be exiled")
    void nonEnchantmentDoesNotTriggerChoice() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        triggerAphemia();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    private void triggerAphemia() {
        harness.addToBattlefield(player1, new AphemiaTheCacophony());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
