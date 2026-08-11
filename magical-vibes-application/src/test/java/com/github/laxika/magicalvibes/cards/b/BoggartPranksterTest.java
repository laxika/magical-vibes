package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoggartPranksterTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, it targets an attacking Goblin you control")
    void targetsAttackingGoblinYouControl() {
        addCreatureReady(player1, new BoggartPrankster());
        Permanent goblin = addCreatureReady(player1, new GoblinPiker());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, goblin);

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .containsExactly(goblin.getId())
                .doesNotContain(bear.getId());

        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(originalPower + 1);
    }

    @Test
    @DisplayName("The trigger is once per combat, even when multiple creatures attack")
    void triggersOncePerCombat() {
        addCreatureReady(player1, new BoggartPrankster());
        Permanent firstGoblin = addCreatureReady(player1, new GoblinPiker());
        Permanent secondGoblin = addCreatureReady(player1, new GoblinPiker());

        declareAttackers(player1, List.of(1, 2));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstGoblin.getId(), secondGoblin.getId());

        harness.handlePermanentChosen(player1, firstGoblin.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstGoblin))
                .isEqualTo(gqs.getEffectivePower(gd, secondGoblin) + 1);
    }
}
