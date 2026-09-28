package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MortuaryMire.class, GrizzlyBears.class, HolyDay.class})
class MortuaryMireTest extends BaseCardTest {

    @Test
    void entersTappedAndMayPutACreatureFromTheGraveyardOnTopOfTheLibrary() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new HolyDay();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent mire = findPermanent(player1, "Mortuary Mire");
        assertThat(mire.isTapped()).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void canDeclineTheGraveyardReturn() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new MortuaryMire()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void tapsForBlackMana() {
        Permanent mire = harness.addToBattlefieldAndReturn(player1, new MortuaryMire());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(mire.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }
}
