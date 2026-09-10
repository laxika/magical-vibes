package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaronHelmutZemo.class, DarkRitual.class, GrizzlyBears.class})
class BaronHelmutZemoTest extends BaseCardTest {

    @Test
    void blackSpellCastFromHandCausesConnive() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        harness.setHand(player1, List.of(new DarkRitual(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(zemo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void boastCopiesTheBlackSpellsPaidForThatActivationAndCastsUpToThree() {
        Permanent zemo = addCreatureReady(player1, new BaronHelmutZemo());
        zemo.setAttackedThisTurn(true);
        List<Card> rituals = IntStream.range(0, 15)
                .mapToObj(ignored -> (Card) new DarkRitual())
                .toList();
        Card nonBlackCard = new GrizzlyBears();
        List<Card> graveyard = new ArrayList<>(rituals);
        graveyard.add(nonBlackCard);
        harness.setGraveyard(player1, graveyard);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ActivatedAbilityGraveyardExileCostChoice costChoice =
                gd.interaction.activeInteraction(PendingInteraction.ActivatedAbilityGraveyardExileCostChoice.class);
        assertThat(costChoice.cards()).containsExactlyElementsOf(rituals);
        harness.handleMultipleCardsChosen(player1, rituals.stream().map(Card::getId).toList());
        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).containsExactlyElementsOf(rituals);

        harness.passBothPriorities();

        PendingInteraction.ImprovisationCapstoneCastChoice castChoice =
                gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
        assertThat(castChoice.validCardIds()).hasSize(15);
        assertThat(castChoice.maxCount()).isEqualTo(3);
        List<UUID> chosenCopies = castChoice.validCardIds().subList(0, 3);
        harness.handleMultipleCardsChosen(player1, chosenCopies);
        resolveAllTriggers();

        assertThat(gd.getCardsExiledByPermanent(zemo.getId())).containsExactlyElementsOf(rituals);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonBlackCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(9);
    }
}
