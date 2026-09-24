package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OthelmSigardianOutcast.class, GrizzlyBears.class})
class OthelmSigardianOutcastTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature put into the graveyard from the battlefield this turn tapped")
    void returnsEligibleCreatureTapped() {
        Permanent othelm = addCreatureReady(player1, new OthelmSigardianOutcast());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Card bearsCard = bears.getCard();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        prepareAbility();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bearsCard.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard().getId()).isEqualTo(bearsCard.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(othelm.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature that was not put into the graveyard from the battlefield this turn")
    void rejectsCreatureNotPutThereFromBattlefieldThisTurn() {
        addCreatureReady(player1, new OthelmSigardianOutcast());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        prepareAbility();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void rejectsOpponentGraveyardCard() {
        addCreatureReady(player1, new OthelmSigardianOutcast());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        prepareAbility();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
