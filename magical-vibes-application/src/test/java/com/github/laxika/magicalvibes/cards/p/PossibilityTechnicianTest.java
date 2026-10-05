package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PossibilityTechnician.class, AlphaKavu.class, GrizzlyBears.class, Murder.class, Conspiracy.class, Forest.class})
@DisplayName("Possibility Technician")
class PossibilityTechnicianTest extends BaseCardTest {

    @Test
    void exilesTheTopCardWhenItOrAnotherKavuEnters() {
        Card first = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first));
        harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);

        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(second));
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);

        harness.enterBattlefieldAndReturn(player1, new AlphaKavu());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    void permissionRequiresControllingAKavuButReactivatesWhenOneReturns() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent technician = harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, technician.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");

        harness.addToBattlefield(player1, new AlphaKavu());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({PossibilityTechnician.class, Conspiracy.class, Forest.class})
    void selfEntryTriggersEvenWhenItsCreatureTypeIsReplaced() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({PossibilityTechnician.class, Forest.class})
    void opposingKavuDoesNotTriggerTheTechnician() {
        harness.addToBattlefield(player1, new PossibilityTechnician());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLibrary(player2, List.of());
        harness.enterBattlefieldAndReturn(player2, new PossibilityTechnician());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({PossibilityTechnician.class})
    void entryWithAnEmptyLibraryDoesNotCauseALoss() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Possibility Technician");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @CardUsed({PossibilityTechnician.class, Forest.class})
    void exiledLandUsesNormalLandTimingAndLandPlayLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());

        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    @CardUsed({PossibilityTechnician.class, Forest.class})
    void exiledCardPermissionContinuesDuringLaterTurns() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player2, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @CardUsed({PossibilityTechnician.class, Forest.class})
    void warpExilesOnResolutionAndAllowsNormalCostRecastOnALaterTurn() {
        PossibilityTechnician technician = new PossibilityTechnician();
        Card first = new Forest();
        Card laterTopCard = new Forest();
        harness.setLibrary(player1, List.of(first, new Forest(), laterTopCard, new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(technician));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Possibility Technician");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Possibility Technician");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(technician);
        assertThatThrownBy(() -> harness.castFromExile(player1, technician.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, technician.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Possibility Technician");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, laterTopCard);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Possibility Technician");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({PossibilityTechnician.class, Murder.class, Forest.class})
    void entryAbilityStillExilesAfterTheTechnicianLeavesBeforeResolution() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent technician = harness.enterBattlefieldAndReturn(player1, new PossibilityTechnician());

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, technician.getId());
        harness.assertNotOnBattlefield(player1, "Possibility Technician");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permission");
    }
}
