package com.github.laxika.magicalvibes.cards.t;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TranscendentDragon.class, GrizzlyBears.class})
class TranscendentDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Cast ETB counters, exiles, and grants a free cast of the target spell")
    void castEtbCountersExilesAndGrantsFreeCast() {
        GrizzlyBears bears = new GrizzlyBears();
        castDragonAgainstSpell(bears);
        harness.handlePermanentChosen(player2, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(bears.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exilePlayPermissions.get(bears.getId())).isEqualTo(player2.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).contains(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player2, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("Rejects a permanent as the ETB target")
    void rejectsPermanentTarget() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new TranscendentDragon()));
        addDragonMana(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDragonAgainstSpell(GrizzlyBears bears) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new TranscendentDragon()));
        addDragonMana(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
    }

    private void addDragonMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.addMana(player, ManaColor.BLUE, 2);
    }
}
