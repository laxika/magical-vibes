package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinMasonsTest extends BaseCardTest {

    @Test
    @DisplayName("When Goblin Masons dies, destroy target Wall")
    void diesDestroysTargetWall() {
        harness.addToBattlefield(player1, new GoblinMasons());
        harness.addToBattlefield(player2, new WallOfWood());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID masonsId = harness.getPermanentId(player1, "Goblin Masons");
        UUID wallId = harness.getPermanentId(player2, "Wall of Wood");

        harness.castInstant(player2, 0, masonsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, wallId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Wood");
        harness.assertInGraveyard(player2, "Wall of Wood");
    }

    @Test
    @DisplayName("Death trigger only offers Walls as valid targets")
    void targetFilterOnlyWalls() {
        harness.addToBattlefield(player1, new GoblinMasons());
        harness.addToBattlefield(player2, new WallOfWood());
        harness.addToBattlefield(player2, new GoblinMasons());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID masonsId = harness.getPermanentId(player1, "Goblin Masons");
        UUID wallId = harness.getPermanentId(player2, "Wall of Wood");

        harness.castInstant(player2, 0, masonsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(wallId);
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
