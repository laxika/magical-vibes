package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoardingRecluse.class, Forest.class, Shock.class, WrathOfGod.class, GroundSeal.class})
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

    @Test
    @DisplayName("Death trigger can choose no target even when a legal target exists")
    void deathTriggerCanChooseNoTarget() {
        HoardingRecluse recluse = new HoardingRecluse();
        Card target = new Shock();
        harness.addToBattlefield(player1, recluse);
        harness.setGraveyard(player2, List.of(target));
        Forest topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        destroyWithWrath();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger can target a land card")
    void deathTriggerCanTargetLand() {
        HoardingRecluse recluse = new HoardingRecluse();
        Forest target = new Forest();
        Forest topCard = new Forest();
        harness.addToBattlefield(player1, recluse);
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(topCard));

        destroyWithWrath();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target);
    }

    @Test
    @DisplayName("Other excludes only the dying Recluse, not another copy in a graveyard")
    void deathTriggerCanTargetAnotherRecluse() {
        HoardingRecluse recluse = new HoardingRecluse();
        HoardingRecluse target = new HoardingRecluse();
        Forest topCard = new Forest();
        harness.addToBattlefield(player1, recluse);
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(topCard));

        destroyWithWrath();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(recluse);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target);
    }

    @Test
    @DisplayName("Death trigger does not move a target that leaves its graveyard before resolution")
    void deathTriggerDoesNotMoveDepartedTarget() {
        HoardingRecluse recluse = new HoardingRecluse();
        Card target = new Shock();
        Forest topCard = new Forest();
        harness.addToBattlefield(player1, recluse);
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(topCard));

        destroyWithWrath();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ground Seal prevents the death trigger from offering graveyard targets")
    void groundSealPreventsGraveyardTargets() {
        harness.addToBattlefield(player1, new HoardingRecluse());
        harness.addToBattlefield(player2, new GroundSeal());
        Card target = new Shock();
        harness.setGraveyard(player2, List.of(target));

        destroyWithWrath();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        if (choice != null) {
            assertThat(choice.validCardIds()).isEmpty();
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
    }

    private void addCreature(Card creature) {
        harness.addToBattlefield(player1, creature);
    }

    private void destroyWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
