package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurGarden;
import com.github.laxika.magicalvibes.cards.h.Halberdier;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScreamsOfTheDamned.class, CentaurGarden.class, Halberdier.class})
class ScreamsOfTheDamnedTest extends BaseCardTest {

    @Test
    void exilesAChosenGraveyardCardAndDealsDamageToEachCreatureAndPlayer() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScreamsOfTheDamned());
        Halberdier ownCreature = new Halberdier();
        Halberdier opponentCreature = new Halberdier();
        addCreatureReady(player1, ownCreature);
        addCreatureReady(player2, opponentCreature);
        Halberdier cardNotChosen = new Halberdier();
        CentaurGarden cardToExile = new CentaurGarden();
        harness.setGraveyard(player1, List.of(cardNotChosen, cardToExile));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cardToExile);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(ownCreature, cardNotChosen)
                .doesNotContain(cardToExile);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
    }

    @Test
    void cannotActivateWithoutACardInTheGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new ScreamsOfTheDamned());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotExileAnOpponentsGraveyardCardAsTheActivationCost() {
        harness.addToBattlefieldAndReturn(player1, new ScreamsOfTheDamned());
        CentaurGarden opponentCard = new CentaurGarden();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(opponentCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
    }
}
