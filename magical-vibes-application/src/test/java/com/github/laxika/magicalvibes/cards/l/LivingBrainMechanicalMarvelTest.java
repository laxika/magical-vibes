package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingBrainMechanicalMarvel.class, DarksteelIngot.class, Bonesplitter.class})
class LivingBrainMechanicalMarvelTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of combat, animates and untaps a target non-Equipment artifact")
    void animatesAndUntapsTargetArtifact() {
        harness.addToBattlefield(player1, new LivingBrainMechanicalMarvel());
        Permanent ingot = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        ingot.tap();

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ingot.getId());

        harness.handlePermanentChosen(player1, ingot.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ingot)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ingot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ingot)).isEqualTo(3);
        assertThat(ingot.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The beginning-of-combat trigger excludes Equipment and opponents' artifacts")
    void onlyTargetsControlledNonEquipmentArtifacts() {
        harness.addToBattlefield(player1, new LivingBrainMechanicalMarvel());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .doesNotContain(equipment.getId(), opponentArtifact.getId());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
