package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreamsOfTheDamnedTest extends BaseCardTest {

    @Test
    void exilesAChosenGraveyardCardAndDealsDamageToEachCreatureAndPlayer() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScreamsOfTheDamned());
        LlanowarElves ownCreature = new LlanowarElves();
        LlanowarElves opponentCreature = new LlanowarElves();
        addCreatureReady(player1, ownCreature);
        addCreatureReady(player2, opponentCreature);
        Shock cardToExile = new Shock();
        harness.setGraveyard(player1, List.of(cardToExile));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(cardToExile);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    void cannotActivateWithoutACardInTheGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new ScreamsOfTheDamned());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
