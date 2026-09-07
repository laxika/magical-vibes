package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AberrantMindSorcerer.class, Shock.class, GrizzlyBears.class})
class AberrantMindSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Targets an instant or sorcery in the controller's graveyard and resolves a roll branch")
    void targetsInstantOrSorceryAndResolvesRollBranch() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AberrantMindSorcerer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
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
    @DisplayName("Does not trigger when the controller has no instant or sorcery in their graveyard")
    void doesNotTriggerWithoutEligibleCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new AberrantMindSorcerer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getId());
    }
}
