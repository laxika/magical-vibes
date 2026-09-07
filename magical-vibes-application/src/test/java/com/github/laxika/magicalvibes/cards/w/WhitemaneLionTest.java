package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhitemaneLion.class, GrizzlyBears.class})
class WhitemaneLionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return a creature you control")
    void etbPromptsCreatureReturn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castLion();

        GameData gd = harness.getGameData();
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID lionId = harness.getPermanentId(player1, "Whitemane Lion");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);

        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bearsId, lionId);
    }

    @Test
    @DisplayName("The chosen creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castLion();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Whitemane Lion");
    }

    @Test
    @DisplayName("The Lion itself can be returned")
    void sourceCanBeReturned() {
        castLion();

        UUID lionId = harness.getPermanentId(player1, "Whitemane Lion");
        harness.handlePermanentChosen(player1, lionId);

        harness.assertInHand(player1, "Whitemane Lion");
        harness.assertNotOnBattlefield(player1, "Whitemane Lion");
    }

    private void castLion() {
        harness.setHand(player1, List.of(new WhitemaneLion()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
