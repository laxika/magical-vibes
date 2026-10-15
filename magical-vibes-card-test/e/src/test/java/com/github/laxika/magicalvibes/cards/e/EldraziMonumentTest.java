package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfJubilation;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EldraziMonument.class, GrizzlyBears.class, LlanowarElves.class,
        IntoTheRoil.class, AngelOfJubilation.class})
class EldraziMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts and grants flying and indestructible to your creatures only")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new EldraziMonument());
        Permanent ours = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ours)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ours)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ours, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, ours, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, theirs)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, theirs, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("At upkeep, the controller chooses a creature to sacrifice")
    void sacrificesAChosenCreatureAtUpkeep() {
        harness.addToBattlefield(player1, new EldraziMonument());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), elves.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Eldrazi Monument");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("At upkeep, sacrifices itself when its controller has no creatures")
    void sacrificesItselfWithoutACreature() {
        harness.addToBattlefield(player1, new EldraziMonument());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eldrazi Monument");
        harness.assertInGraveyard(player1, "Eldrazi Monument");
    }

    @Test
    @DisplayName("A lone indestructible creature must be sacrificed instead of the Monument")
    void sacrificesItsOnlyCreature() {
        harness.addToBattlefield(player1, new EldraziMonument());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Eldrazi Monument");
        harness.assertNotInGraveyard(player1, "Eldrazi Monument");
    }

    @Test
    @DisplayName("The upkeep ability does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new EldraziMonument());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Eldrazi Monument");
        harness.assertNotInGraveyard(player1, "Eldrazi Monument");
    }

    @Test
    @DisplayName("Removing the Monument ends its bonuses but does not stop its upkeep sacrifice")
    void upkeepTriggerSurvivesSourceRemoval() {
        Permanent monument = harness.addToBattlefieldAndReturn(player1, new EldraziMonument());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IntoTheRoil()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, monument.getId());

        harness.assertInHand(player1, "Eldrazi Monument");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();

        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Eldrazi Monument");
    }

    @Test
    @DisplayName("Angel of Jubilation does not prevent sacrificing a creature to the upkeep effect")
    void sacrificeDuringResolutionIsNotAnActivationCost() {
        harness.addToBattlefield(player1, new EldraziMonument());
        harness.addToBattlefield(player1, new AngelOfJubilation());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angel of Jubilation");
        harness.assertNotOnBattlefield(player1, "Angel of Jubilation");
        harness.assertOnBattlefield(player1, "Eldrazi Monument");
        harness.assertNotInGraveyard(player1, "Eldrazi Monument");
    }
}
