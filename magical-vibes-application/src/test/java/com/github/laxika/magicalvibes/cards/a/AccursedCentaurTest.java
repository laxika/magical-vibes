package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AccursedCentaur.class, ElvishWarrior.class, GlorySeeker.class, Shock.class})
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

    @Test
    @DisplayName("Its controller may choose the Centaur even when another creature is available")
    void choosesItselfToSacrifice() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        castAccursedCentaur();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Accursed Centaur"));

        harness.assertInGraveyard(player1, "Accursed Centaur");
        harness.assertOnBattlefield(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("The sacrifice trigger still resolves after the Centaur dies in response")
    void sacrificesAnotherCreatureAfterSourceDies() {
        harness.addToBattlefield(player1, new ElvishWarrior());
        castAccursedCentaur();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Accursed Centaur"));
        harness.assertInGraveyard(player1, "Accursed Centaur");
        harness.assertOnBattlefield(player1, "Elvish Warrior");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The trigger completes without a choice when no creatures remain")
    void noCreaturesRemainAfterSourceDies() {
        harness.addToBattlefield(player2, new GlorySeeker());
        castAccursedCentaur();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Accursed Centaur"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Accursed Centaur");
        harness.assertOnBattlefield(player2, "Glory Seeker");
        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Entering without being cast still triggers the sacrifice")
    void triggersWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player1, new AccursedCentaur());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Accursed Centaur");
        harness.assertNotOnBattlefield(player1, "Accursed Centaur");
    }

    private void castAccursedCentaur() {
        harness.castFromHand(player1, new AccursedCentaur(), "{B}");
    }
}
