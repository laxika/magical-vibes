package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccursedCentaur.class, GrizzlyBears.class, GiantSpider.class})
@DisplayName("Accursed Centaur")
class AccursedCentaurTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller sacrifices it when it is their only creature")
    void sacrificesOnlyCreature() {
        castAccursedCentaur();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Accursed Centaur");
        harness.assertInGraveyard(player1, "Accursed Centaur");
    }

    @Test
    @DisplayName("Its controller chooses a creature when they control more than one")
    void choosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());
        castAccursedCentaur();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Accursed Centaur");
        harness.assertOnBattlefield(player1, "Giant Spider");
    }

    private void castAccursedCentaur() {
        harness.setHand(player1, List.of(new AccursedCentaur()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
    }
}
