package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SphinxOfForgottenLoreTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets an instant or sorcery card in its controller's graveyard")
    void attackTargetsInstantOrSorcery() {
        Card instant = new Shock();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, creature));
        addReadySphinx();

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).containsExactly(instant.getId());
    }

    @Test
    @DisplayName("Granted flashback uses the card's mana cost and exiles the card after casting")
    void grantsFlashbackWithManaCost() {
        Card instant = new Shock();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(instant));
        addReadySphinx();

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(instant.getId()));
    }

    @Test
    @DisplayName("The attack trigger does not trigger without an instant or sorcery in its controller's graveyard")
    void noLegalTargetSkipsTrigger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock()));
        addReadySphinx();

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySphinx() {
        return addCreatureReady(player1, new SphinxOfForgottenLore());
    }
}
