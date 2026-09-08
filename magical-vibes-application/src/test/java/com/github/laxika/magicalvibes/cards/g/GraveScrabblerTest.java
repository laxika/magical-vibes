package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveScrabbler.class, GrizzlyBears.class, HolyDay.class, RavensCrime.class})
class GraveScrabblerTest extends BaseCardTest {

    @Test
    @DisplayName("A normal cast does not trigger the madness ability")
    void normalCastDoesNotTriggerMadnessAbility() {
        GraveScrabbler scrabbler = new GraveScrabbler();
        harness.setHand(player1, List.of(scrabbler));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(scrabbler.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Madness ETB may return a target creature from any graveyard to its owner's hand")
    void madnessEtbReturnsCreatureToItsOwnersHand() {
        Card nonCreature = new HolyDay();
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(nonCreature, target));

        castForMadness();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Declining the madness ETB leaves the targeted card in its graveyard")
    void decliningMadnessEtbLeavesTargetInGraveyard() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        castForMadness();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(target);
    }

    private void castForMadness() {
        GraveScrabbler scrabbler = new GraveScrabbler();
        harness.setHand(player1, List.of(scrabbler));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }
}
