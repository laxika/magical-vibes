package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.cards.w.WallOfGlare;
import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinMasons.class, WallOfGlare.class, HulkingOgre.class, RecklessAbandon.class})
class GoblinMasonsTest extends BaseCardTest {

    @Test
    @DisplayName("When Goblin Masons dies, destroy target Wall")
    void diesDestroysTargetWall() {
        Permanent masons = addCreatureReady(player1, new GoblinMasons());
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        Permanent sacrifice = addCreatureReady(player2, new HulkingOgre());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new RecklessAbandon()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player2, 0, masons.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Glare");
        harness.assertInGraveyard(player2, "Wall of Glare");
    }

    @Test
    @DisplayName("Death trigger only offers Walls as valid targets")
    void targetFilterOnlyWalls() {
        Permanent masons = addCreatureReady(player1, new GoblinMasons());
        Permanent wall = addCreatureReady(player2, new WallOfGlare());
        Permanent nonWall = addCreatureReady(player2, new HulkingOgre());
        Permanent sacrifice = addCreatureReady(player2, new WallOfGlare());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new RecklessAbandon()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player2, 0, masons.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(wall.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(nonWall);
    }

    @Test
    @DisplayName("Death trigger is skipped when no Wall remains")
    void noTriggerWhenNoWallIsAvailable() {
        Permanent masons = addCreatureReady(player1, new GoblinMasons());
        Permanent sacrifice = addCreatureReady(player2, new HulkingOgre());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new RecklessAbandon()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player2, 0, masons.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Masons");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
