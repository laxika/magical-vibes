package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HavengulSkaab.class, GrizzlyBears.class, Island.class})
class HavengulSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking prompts the controller to return another creature they control")
    void attackingPromptsBounceOfAnotherCreature() {
        addCreatureReady(player1, new HavengulSkaab());
        addCreatureReady(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        declareAttackers(List.of(0));
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bearsId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen creature is returned to its owner's hand")
    void chosenCreatureReturnsToHand() {
        addCreatureReady(player1, new HavengulSkaab());
        addCreatureReady(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, bearsId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Havengul Skaab");
    }

    @Test
    @DisplayName("The Skaab itself and non-creatures are not valid choices")
    void skaabAndNoncreaturesExcluded() {
        addCreatureReady(player1, new HavengulSkaab());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        declareAttackers(List.of(0));
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bearsId);
    }

    @Test
    @DisplayName("Creatures the opponent controls are never returned")
    void opponentCreaturesNotChoices() {
        addCreatureReady(player1, new HavengulSkaab());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("With no other creature, nothing happens")
    void noOtherCreatureNoBounce() {
        addCreatureReady(player1, new HavengulSkaab());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Havengul Skaab");
    }

    @Test
    @DisplayName("Another Havengul Skaab is a valid choice even though it has the same name")
    void anotherSkaabCanBeReturned() {
        var attacker = addCreatureReady(player1, new HavengulSkaab());
        var other = addCreatureReady(player1, new HavengulSkaab());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(other.getId());
        harness.handlePermanentChosen(player1, other.getId());

        assertThat(findPermanents(player1, "Havengul Skaab")).containsExactly(attacker);
        harness.assertInHand(player1, "Havengul Skaab");
    }

    @Test
    @DisplayName("A controlled creature owned by the opponent returns to the opponent's hand")
    void controlledCreatureReturnsToItsActualOwner() {
        addCreatureReady(player1, new HavengulSkaab());
        GrizzlyBears borrowed = new GrizzlyBears();
        borrowed.setOwnerId(player2.getId());
        var permanent = addCreatureReady(player1, borrowed);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, permanent.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }
}
