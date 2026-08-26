package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GripOfAmnesia.class, GrizzlyBears.class, Forest.class})
class GripOfAmnesiaTest extends BaseCardTest {

    @Test
    void exilesTargetSpellControllersGraveyardAndDrawsWithoutCountering() {
        Card graveyardCard = new Forest();
        Card drawCard = new Forest();
        GrizzlyBears spell = castAgainstOpponent(List.of(graveyardCard), drawCard);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell);
    }

    @Test
    void declinesToExileAndCountersTheSpellThenDraws() {
        Card graveyardCard = new Forest();
        Card drawCard = new Forest();
        castAgainstOpponent(List.of(graveyardCard), drawCard);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(graveyardCard);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(graveyardCard)
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
    }

    @Test
    void mayExileAnEmptyGraveyardAndLeaveTheSpellUncountered() {
        Card drawCard = new Forest();
        GrizzlyBears spell = castAgainstOpponent(List.of(), drawCard);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == spell);
    }

    private GrizzlyBears castAgainstOpponent(List<Card> graveyard, Card drawCard) {
        GrizzlyBears spell = new GrizzlyBears();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player2, graveyard);
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player2, List.of(spell));
        harness.setHand(player1, List.of(new GripOfAmnesia()));

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, 0, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        return spell;
    }
}
