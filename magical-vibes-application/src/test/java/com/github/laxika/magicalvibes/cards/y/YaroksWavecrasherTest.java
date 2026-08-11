package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YaroksWavecrasherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts to return another creature you control")
    void etbPromptsBounceOfAnotherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YaroksWavecrasher()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(bearsId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("The chosen creature returns to its owner's hand")
    void chosenCreatureReturnsToHand() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YaroksWavecrasher()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Yarok's Wavecrasher");
    }

    @Test
    @DisplayName("The source and noncreatures are not valid choices")
    void sourceAndNoncreaturesExcluded() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new YaroksWavecrasher()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Yarok's Wavecrasher");
    }
}
