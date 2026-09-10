package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EnsnaringBridge;
import com.github.laxika.magicalvibes.cards.m.MoggFlunkies;
import com.github.laxika.magicalvibes.cards.n.NomadsEnKor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowstoneBlade.class, MoggFlunkies.class, NomadsEnKor.class, EnsnaringBridge.class})
class FlowstoneBladeTest extends BaseCardTest {

    @Test
    void activatedAbilityBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new MoggFlunkies());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FlowstoneBlade());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void activatedAbilityBoostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new MoggFlunkies());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FlowstoneBlade());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void activatedAbilityCanBeUsedMultipleTimes() {
        Permanent creature = addCreatureReady(player1, new MoggFlunkies());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FlowstoneBlade());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void resolvingAttachesToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MoggFlunkies());
        harness.setHand(player1, List.of(new FlowstoneBlade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof FlowstoneBlade
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    void negativeToughnessFromAbilityCausesCreatureToDie() {
        Permanent creature = addCreatureReady(player1, new NomadsEnKor());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FlowstoneBlade());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nomads en-Kor");
        harness.assertInGraveyard(player1, "Nomads en-Kor");
    }

    @Test
    void cannotEnchantANoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnsnaringBridge());
        harness.setHand(player1, List.of(new FlowstoneBlade()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
