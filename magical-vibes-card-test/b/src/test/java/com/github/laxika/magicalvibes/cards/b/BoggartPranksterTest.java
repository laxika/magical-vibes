package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoggartPrankster.class, GoblinPiker.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("It can boost itself and the boost lasts until cleanup")
    void boostsItselfUntilCleanup() {
        Permanent prankster = addCreatureReady(player1, new BoggartPrankster());
        int originalPower = gqs.getEffectivePower(gd, prankster);
        int originalToughness = gqs.getEffectiveToughness(gd, prankster);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, prankster.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prankster)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, prankster)).isEqualTo(originalToughness);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, prankster)).isEqualTo(originalPower + 1);
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, prankster)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, prankster)).isEqualTo(originalToughness);
    }

    @Test
    @DisplayName("Attacking only with a non-Goblin gives no valid target")
    void noTargetWhenNoGoblinAttacks() {
        Permanent prankster = addCreatureReady(player1, new BoggartPrankster());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, bear);

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(originalPower);
        assertThat(prankster.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("An opponent's attack does not trigger the ability")
    void doesNotTriggerForOpponentAttack() {
        addCreatureReady(player1, new BoggartPrankster());
        Permanent goblin = addCreatureReady(player2, new GoblinPiker());
        int originalPower = gqs.getEffectivePower(gd, goblin);

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(originalPower);
    }

    @Test
    @DisplayName("The target must still be attacking when the ability resolves")
    void targetRemovedFromCombatIsIllegal() {
        Permanent prankster = addCreatureReady(player1, new BoggartPrankster());
        int originalPower = gqs.getEffectivePower(gd, prankster);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, prankster.getId());
        prankster.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prankster)).isEqualTo(originalPower);
    }
}
