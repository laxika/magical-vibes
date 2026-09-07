package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NarcolepsyTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the untapped enchanted creature during upkeep")
    void tapsUntappedEnchantedCreatureDuringUpkeep() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAttachedAura(bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger for a tapped enchanted creature")
    void doesNotTriggerForTappedEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();
        addAttachedAura(bears);
        advanceToUpkeepWithoutUntapping(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Triggers during the enchanted creature controller's upkeep")
    void triggersDuringEnchantedCreatureControllersUpkeep() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addAttachedAura(bears);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    private void addAttachedAura(Permanent creature) {
        Permanent aura = new Permanent(new Narcolepsy());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void advanceToUpkeepWithoutUntapping(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.UPKEEP);
    }
}
