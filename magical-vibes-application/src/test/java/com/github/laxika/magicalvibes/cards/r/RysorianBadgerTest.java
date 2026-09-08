package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrySpell;
import com.github.laxika.magicalvibes.cards.s.SeaTroll;
import com.github.laxika.magicalvibes.cards.s.SpectralBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RysorianBadger.class, SpectralBears.class, SeaTroll.class, DrySpell.class})
class RysorianBadgerTest extends BaseCardTest {

    private void addAttacker() {
        addCreatureReady(player1, new RysorianBadger());
    }

    private List<Card> setDefenderGraveyard() {
        List<Card> cards = List.of(new SpectralBears(), new SeaTroll(), new DrySpell());
        harness.setGraveyard(player2, cards);
        return cards;
    }

    private void attackUnblocked() {
        declareAttackers(List.of(0));
    }

    private List<UUID> validChoiceIds() {
        return new ArrayList<>(gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
    }

    @Test
    @DisplayName("Only creature cards in the defending player's graveyard can be chosen")
    void onlyDefendersCreatureCardsAreChoosable() {
        List<Card> defenderCards = setDefenderGraveyard();
        harness.setGraveyard(player1, List.of(new SpectralBears()));
        addAttacker();

        attackUnblocked();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        List<UUID> defenderCreatureIds = defenderCards.subList(0, 2).stream()
                .map(Card::getId)
                .toList();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(defenderCreatureIds);
    }

    @Test
    @DisplayName("Exiling two creature cards gains 2 life and the badger deals no combat damage")
    void exilingTwoGainsLifeAndPreventsCombatDamage() {
        List<Card> defenderCards = setDefenderGraveyard();
        addAttacker();

        attackUnblocked();

        harness.handleMultipleCardsChosen(player1, validChoiceIds());
        harness.passBothPriorities(); // resolve the trigger

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(defenderCards.get(2));
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Exiling a single creature card gains only 1 life")
    void exilingOneGainsOneLife() {
        List<Card> defenderCards = setDefenderGraveyard();
        addAttacker();

        attackUnblocked();

        harness.handleMultipleCardsChosen(player1, List.of(validChoiceIds().getFirst()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(defenderCards.get(1), defenderCards.get(2));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("A selected card that leaves the graveyard before resolution does not apply the optional riders")
    void targetLeavingGraveyardBeforeResolutionDoesNotApplyRiders() {
        List<Card> defenderCards = setDefenderGraveyard();
        addAttacker();

        attackUnblocked();

        harness.handleMultipleCardsChosen(player1, List.of(defenderCards.getFirst().getId()));
        harness.setGraveyard(player2, defenderCards.subList(1, defenderCards.size()));
        harness.setGraveyard(player1, List.of(defenderCards.getFirst()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(defenderCards.get(1), defenderCards.get(2));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(defenderCards.getFirst());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Choosing no cards gains no life and the badger still deals combat damage")
    void choosingNoCardsLeavesCombatDamageIntact() {
        List<Card> defenderCards = setDefenderGraveyard();
        addAttacker();

        attackUnblocked();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(defenderCards);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("No creature card in the defending player's graveyard presents no choice")
    void noCreatureCardsPresentsNoChoice() {
        Card drySpell = new DrySpell();
        harness.setGraveyard(player2, List.of(drySpell));
        addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(drySpell);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A blocked badger does not trigger at all")
    void blockedBadgerDoesNotTrigger() {
        List<Card> defenderCards = setDefenderGraveyard();
        addCreatureReady(player2, new SeaTroll());

        addAttacker();
        declareAttackers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(defenderCards);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
