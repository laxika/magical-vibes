package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.s.ShockingGrasp;
import com.github.laxika.magicalvibes.cards.y.YouFindACursedIdol;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AberrantMindSorcerer.class, ShockingGrasp.class, HillGiantHerdgorger.class,
        YouFindACursedIdol.class})
class AberrantMindSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Targets an instant or sorcery in the controller's graveyard and resolves a roll branch")
    void targetsInstantOrSorceryAndResolvesRollBranch() {
        ShockingGrasp shock = new ShockingGrasp();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.castFromHand(player1, new AberrantMindSorcerer(), "{4}{U}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(shock.getId());

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(shock.getId());
        } else {
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(card -> card.getId().equals(shock.getId()));
        }
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Has no target choice when the controller has no eligible graveyard card")
    void noTargetChoiceWithoutEligibleCard() {
        HillGiantHerdgorger bears = new HillGiantHerdgorger();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new AberrantMindSorcerer(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Includes sorceries but excludes creatures and cards in opposing graveyards")
    void targetsSorceryFromMixedGraveyard() {
        YouFindACursedIdol sorcery = new YouFindACursedIdol();
        ShockingGrasp instant = new ShockingGrasp();
        HillGiantHerdgorger creature = new HillGiantHerdgorger();
        ShockingGrasp opposingInstant = new ShockingGrasp();
        harness.setGraveyard(player1, List.of(sorcery, instant, creature));
        harness.setGraveyard(player2, List.of(opposingInstant));
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));

        harness.castFromHand(player1, new AberrantMindSorcerer(), "{4}{U}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(sorcery.getId(), instant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, false);
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(sorcery);
            assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(sorcery);
            assertThat(gd.playerHands.get(player1.getId())).doesNotContain(sorcery);
        } else {
            assertThat(gd.playerHands.get(player1.getId())).contains(sorcery);
            assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(sorcery);
        }
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant, creature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingInstant);
    }

    @Test
    @DisplayName("Does not retrieve a different card when the chosen target leaves the graveyard")
    void targetLeavesBeforeResolution() {
        ShockingGrasp target = new ShockingGrasp();
        YouFindACursedIdol other = new YouFindACursedIdol();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setLibrary(player1, List.of(new HillGiantHerdgorger()));
        harness.castFromHand(player1, new AberrantMindSorcerer(), "{4}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target, other);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(target, other);
    }
}
