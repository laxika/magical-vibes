package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImperiousPerfect;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChangelingTitan.class, GrizzlyBears.class, Unsummon.class, ImperiousPerfect.class})
class ChangelingTitanTest extends BaseCardTest {

    private void castChangelingTitan() {
        harness.castFromHand(player1, new ChangelingTitan(), "{4}{G}");
        harness.passBothPriorities(); // resolve creature spell -> ETB on stack
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has no other creatures")
    void autoSacrificesWithNoOtherCreatures() {
        castChangelingTitan();
        harness.passBothPriorities(); // resolve champion ETB -> auto-sacrifice

        harness.assertNotOnBattlefield(player1, "Changeling Titan");
        harness.assertInGraveyard(player1, "Changeling Titan");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Championing a creature exiles it and keeps Changeling Titan")
    void championingExilesCreatureAndKeepsTitan() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castChangelingTitan();
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertOnBattlefield(player1, "Changeling Titan");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Championed creature returns when Changeling Titan leaves the battlefield")
    void championedCreatureReturnsWhenTitanLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castChangelingTitan();
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID titanId = harness.getPermanentId(player1, "Changeling Titan");
        harness.castInstant(player1, 0, titanId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Changeling Titan");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        harness.passBothPriorities(); // resolve the champion leave trigger

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Opponent's creatures cannot satisfy champion")
    void doesNotChampionOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castChangelingTitan();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Changeling Titan");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Champion excludes Titan itself and offers only the other controlled creature")
    void championChoiceExcludesSelfAndOpponent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        castChangelingTitan();
        harness.passBothPriorities();

        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(bearsId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Champion can still exile a creature after Titan has left")
    void championCanExileAfterTitanLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        castChangelingTitan();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Changeling Titan"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertInHand(player1, "Changeling Titan");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Changeling receives the bonus for Elves from Imperious Perfect")
    void changelingReceivesElfLordBonus() {
        harness.addToBattlefield(player1, new ImperiousPerfect());
        harness.addToBattlefield(player1, new ChangelingTitan());

        var titan = findPermanent(player1, "Changeling Titan");
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, titan)).isEqualTo(8);
    }
}
