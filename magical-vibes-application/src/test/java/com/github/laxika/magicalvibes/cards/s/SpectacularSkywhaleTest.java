package com.github.laxika.magicalvibes.cards.s;

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
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpectacularSkywhaleTest extends BaseCardTest {

    private Permanent addSkywhale(Player player) {
        Permanent perm = new Permanent(new SpectacularSkywhale());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Has spell-cast trigger with a +3/+0-or-three-counters ConditionalReplacementEffect")
    void hasCorrectEffects() {
        SpectacularSkywhale card = new SpectacularSkywhale();

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();

        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(ConditionalReplacementEffect.class);
        ConditionalReplacementEffect replacement =
                (ConditionalReplacementEffect) trigger.resolvedEffects().getFirst();
        assertThat(((SpellManaSpentAtLeast) replacement.condition()).minMana()).isEqualTo(5);

        BoostSelfEffect boost = (BoostSelfEffect) replacement.baseEffect();
        assertThat(boost.powerBoost()).isEqualTo(3);
        assertThat(boost.toughnessBoost()).isEqualTo(0);

        PutCountersOnSelfEffect counters = (PutCountersOnSelfEffect) replacement.upgradedEffect();
        assertThat(counters.counterType()).isEqualTo(CounterType.PLUS_ONE_PLUS_ONE);
        assertThat(counters.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a cheap instant grants +3/+0 until end of turn and no counters")
    void cheapSpellBoostsWithoutCounters() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isEqualTo(3);
        assertThat(skywhale.getToughnessModifier()).isZero();
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a four-mana spell grants +3/+0 and no counters (below threshold)")
    void fourManaSpellBoostsWithoutCounters() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isEqualTo(3);
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a five-mana spell puts three +1/+1 counters instead of boosting")
    void fiveManaSpellAddsThreeCounters() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(skywhale.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isZero();
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
