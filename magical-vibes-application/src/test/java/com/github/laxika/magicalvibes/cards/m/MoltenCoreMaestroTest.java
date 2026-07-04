package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.effect.AwardManaEqualToSourcePowerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MoltenCoreMaestroTest extends BaseCardTest {

    private Permanent addMaestro(Player player) {
        MoltenCoreMaestro card = new MoltenCoreMaestro();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Has spell-cast trigger with self counter and conditional mana-equal-to-power")
    void hasCorrectEffects() {
        MoltenCoreMaestro card = new MoltenCoreMaestro();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();

        assertThat(trigger.resolvedEffects()).hasSize(2);
        assertThat(trigger.resolvedEffects().get(0)).isInstanceOf(PutCountersOnSelfEffect.class);
        PutCountersOnSelfEffect counter = (PutCountersOnSelfEffect) trigger.resolvedEffects().get(0);
        assertThat(counter.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);

        assertThat(trigger.resolvedEffects().get(1)).isInstanceOf(ConditionalEffect.class);
        ConditionalEffect conditional = (ConditionalEffect) trigger.resolvedEffects().get(1);
        assertThat(((SpellManaSpentAtLeast) conditional.condition()).minMana()).isEqualTo(5);
        assertThat(conditional.wrapped()).isInstanceOf(AwardManaEqualToSourcePowerEffect.class);
        AwardManaEqualToSourcePowerEffect mana = (AwardManaEqualToSourcePowerEffect) conditional.wrapped();
        assertThat(mana.color()).isEqualTo(ManaColor.RED);
    }

    @Test
    @DisplayName("Casting a cheap instant adds a +1/+1 counter but no mana")
    void cheapSpellAddsCounterOnly() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Cast spent the single red mana; no mana was added by the trigger.
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Casting a four-mana spell adds a counter but no mana")
    void fourManaSpellAddsCounterOnly() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Casting a five-mana spell adds a counter and red mana equal to power (including the new counter)")
    void fiveManaSpellAddsCounterAndMana() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Base power 2 + the freshly-added +1/+1 counter = 3 red mana.
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
