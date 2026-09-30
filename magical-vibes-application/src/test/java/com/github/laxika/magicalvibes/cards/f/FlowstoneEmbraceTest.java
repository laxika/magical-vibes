package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GroveOfTheBurnwillows;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneEmbrace.class, FomoriNomad.class, GroveOfTheBurnwillows.class})
class FlowstoneEmbraceTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsEnchantedCreature() {
        Permanent giant = addCreatureReady(player1, new FomoriNomad());
        attachFlowstoneEmbrace(player1, giant);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);
    }

    @Test
    void activatedAbilityBoostWearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player1, new FomoriNomad());
        attachFlowstoneEmbrace(player1, giant);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    void resolvingAuraAttachesToTargetCreature() {
        Permanent creature = addCreatureReady(player1, new FomoriNomad());
        harness.setHand(player1, List.of(new FlowstoneEmbrace()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Flowstone Embrace");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void activatedAbilityBoostsOpponentsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new FomoriNomad());
        attachFlowstoneEmbrace(player1, creature);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void cannotEnchantALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GroveOfTheBurnwillows());
        harness.setHand(player1, List.of(new FlowstoneEmbrace()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachFlowstoneEmbrace(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new FlowstoneEmbrace());
        aura.setAttachedTo(creature.getId());
    }
}
