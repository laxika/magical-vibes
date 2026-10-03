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
        harness.withAutoStop(TurnStep.END_STEP, () ->
                harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId())));
        assertThat(gd.stack).isEmpty();

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

    @Test
    @DisplayName("Aphemia can exile itself after dying in response to its trigger")
    void canExileItselfAfterDyingInResponse() {
        AphemiaTheCacophony aphemia = new AphemiaTheCacophony();
        var permanent = harness.addToBattlefieldAndReturn(player1, aphemia);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        permanent.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aphemia);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(aphemia.getId());
        harness.withAutoStop(TurnStep.END_STEP, () ->
                harness.handleMultipleCardsChosen(player1, List.of(aphemia.getId())));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(aphemia);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Only one of multiple enchantments is exiled for one Zombie")
    void choosesOneOfMultipleEnchantments() {
        Card first = new OmenOfTheSea();
        Card second = new OmenOfTheSea();
        harness.setGraveyard(player1, List.of(first, second));
        triggerAphemia();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.withAutoStop(TurnStep.END_STEP, () ->
                harness.handleMultipleCardsChosen(player1, List.of(second.getId())));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .hasSize(1);
    }

    @Test
    @DisplayName("An opponent's enchantment cannot be exiled")
    void cannotExileOpponentsEnchantment() {
        Card enchantment = new OmenOfTheSea();
        harness.setGraveyard(player2, List.of(enchantment));
        triggerAphemia();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(enchantment);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    @Test
    @DisplayName("Aphemia does not trigger during its opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new AphemiaTheCacophony());
        Card enchantment = new OmenOfTheSea();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
    }

    private void triggerAphemia() {
        harness.addToBattlefield(player1, new AphemiaTheCacophony());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());
    }
}
