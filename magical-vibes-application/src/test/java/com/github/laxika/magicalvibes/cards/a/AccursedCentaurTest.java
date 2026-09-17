package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccursedCentaur.class, ElvishWarrior.class, GlorySeeker.class})
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
    @DisplayName("Its controller sacrifices their creature rather than an opponent's creature")
    void sacrificesOnlyControllerCreature() {
        harness.addToBattlefield(player2, new GlorySeeker());
        castAccursedCentaur();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Accursed Centaur");
        harness.assertOnBattlefield(player2, "Glory Seeker");
    }

    @Test
    @DisplayName("Its controller chooses a creature when they control more than one")
    void choosesCreatureToSacrifice() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new GlorySeeker());
        castAccursedCentaur();
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Elvish Warrior"));

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player1, "Accursed Centaur");
        harness.assertOnBattlefield(player1, "Glory Seeker");
    }

    private void castAccursedCentaur() {
        harness.castFromHand(player1, new AccursedCentaur(), "{B}");
    }
}
