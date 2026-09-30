package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.o.OrzhovSignet;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarrierGriffin.class, GhostWarden.class, OrzhovSignet.class})
class HarrierGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger targets and taps a creature")
    void upkeepTriggerTargetsAndTapsCreature() {
        addCreatureReady(player1, new HarrierGriffin());
        Permanent target = addCreatureReady(player2, new GhostWarden());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep trigger may target a creature controlled by the Griffin's controller")
    void upkeepTriggerMayTargetOwnCreature() {
        addCreatureReady(player1, new HarrierGriffin());
        Permanent target = addCreatureReady(player1, new GhostWarden());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep trigger cannot target a noncreature permanent")
    void upkeepTriggerCannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new HarrierGriffin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OrzhovSignet());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).doesNotContain(artifact.getId());
    }
}
