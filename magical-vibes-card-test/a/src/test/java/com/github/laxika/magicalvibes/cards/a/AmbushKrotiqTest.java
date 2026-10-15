package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.cards.c.CrucibleOfTheSpiritDragon;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmbushKrotiq.class, FeralKrushok.class, CrucibleOfTheSpiritDragon.class})
class AmbushKrotiqTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return another creature you control")
    void etbPromptsBounceOfAnotherCreature() {
        harness.addToBattlefield(player1, new FeralKrushok());
        UUID krushokId = harness.getPermanentId(player1, "Feral Krushok");

        harness.castFromHand(player1, new AmbushKrotiq(), "{5}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(krushokId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        addCreatureReady(player1, new FeralKrushok());
        UUID krushokId = harness.getPermanentId(player1, "Feral Krushok");

        harness.castFromHand(player1, new AmbushKrotiq(), "{5}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, krushokId);

        harness.assertNotOnBattlefield(player1, "Feral Krushok");
        harness.assertInHand(player1, "Feral Krushok");
        harness.assertOnBattlefield(player1, "Ambush Krotiq");
    }

    @Test
    @DisplayName("The source and noncreatures are not valid choices")
    void sourceAndNoncreaturesExcluded() {
        harness.addToBattlefield(player1, new CrucibleOfTheSpiritDragon());

        harness.castFromHand(player1, new AmbushKrotiq(), "{5}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Crucible of the Spirit Dragon");
        harness.assertOnBattlefield(player1, "Ambush Krotiq");
    }

    @Test
    @DisplayName("Opponent's creatures cannot be returned")
    void opponentsCreaturesAreExcluded() {
        harness.addToBattlefield(player2, new FeralKrushok());

        harness.castFromHand(player1, new AmbushKrotiq(), "{5}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Feral Krushok");
        harness.assertOnBattlefield(player1, "Ambush Krotiq");
    }

    @Test
    @DisplayName("Another Ambush Krotiq is a valid choice")
    void anotherKrotiqCanBeReturned() {
        harness.addToBattlefield(player1, new AmbushKrotiq());
        UUID otherId = harness.getPermanentId(player1, "Ambush Krotiq");

        harness.castFromHand(player1, new AmbushKrotiq(), "{5}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(otherId);
        harness.handlePermanentChosen(player1, otherId);

        assertThat(countPermanents(player1, "Ambush Krotiq")).isEqualTo(1);
        assertThat(findPermanent(player1, "Ambush Krotiq").getId()).isNotEqualTo(otherId);
        harness.assertInHand(player1, "Ambush Krotiq");
    }

    @Test
    @DisplayName("A controlled creature returns to its owner rather than its controller")
    void borrowedCreatureReturnsToOwner() {
        FeralKrushok borrowed = new FeralKrushok();
        borrowed.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, borrowed);
        UUID borrowedId = harness.getPermanentId(player1, "Feral Krushok");

        harness.castFromHand(player1, new AmbushKrotiq(), "{5}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, borrowedId);

        harness.assertNotOnBattlefield(player1, "Feral Krushok");
        harness.assertNotInHand(player1, "Feral Krushok");
        harness.assertInHand(player2, "Feral Krushok");
        harness.assertOnBattlefield(player1, "Ambush Krotiq");
    }
}
