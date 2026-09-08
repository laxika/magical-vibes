package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HoardingRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger puts a card from your graveyard on the bottom of its owner's library")
    void deathTriggerTucksCardFromOwnGraveyard() {
        HoardingRecluse recluse = new HoardingRecluse();
        Card target = new Shock();
        addCreature(recluse);
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest())));

        destroyWithWrath();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(recluse)
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(target);
    }

    @Test
    @DisplayName("Death trigger puts a card from an opponent's graveyard on that owner's library")
    void deathTriggerTucksCardFromOpponentGraveyard() {
        HoardingRecluse recluse = new HoardingRecluse();
        Card target = new Shock();
        addCreature(recluse);
        harness.setGraveyard(player2, new ArrayList<>(List.of(target)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Forest())));

        destroyWithWrath();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getLast()).isSameAs(target);
    }

    @Test
    @DisplayName("Death trigger excludes the dying Hoarding Recluse from its target choices")
    void deathTriggerExcludesItself() {
        HoardingRecluse recluse = new HoardingRecluse();
        addCreature(recluse);

        destroyWithWrath();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).doesNotContain(recluse.getId());
    }

    private void addCreature(Card creature) {
        harness.addToBattlefield(player1, creature);
    }

    private void destroyWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
