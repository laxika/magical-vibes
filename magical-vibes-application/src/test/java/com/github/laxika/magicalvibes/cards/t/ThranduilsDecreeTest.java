package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranduilsDecree.class, GrizzlyBears.class, Divination.class})
class ThranduilsDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a permanent spell, exiles it, and lets its controller cast it for free")
    void countersPermanentSpellAndGrantsFreeCastPermission() {
        GrizzlyBears bears = new GrizzlyBears();
        castDecreeAgainstCreature(bears);

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(bears);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("Counters a nonpermanent spell into its owner's graveyard")
    void countersNonpermanentSpellIntoGraveyard() {
        Divination divination = new Divination();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(divination));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setHand(player1, List.of(new ThranduilsDecree()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player2, 0, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, divination.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Divination");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(divination.getId());
    }

    private void castDecreeAgainstCreature(GrizzlyBears bears) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new ThranduilsDecree()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
    }
}
