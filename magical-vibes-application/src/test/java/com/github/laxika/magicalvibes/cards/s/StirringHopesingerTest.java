package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StirringHopesingerTest extends BaseCardTest {

    @Test
    @DisplayName("Repartee trigger puts a counter on each controlled creature, gated on targeting a creature")
    void hasCorrectStructure() {
        StirringHopesinger card = new StirringHopesinger();

        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.castSpellTargetCondition()).isInstanceOf(StackEntryTargetsPermanentPredicate.class);
        assertThat(trigger.resolvedEffects()).singleElement()
                .isInstanceOf(PutPlusOnePlusOneCounterOnEachControlledPermanentEffect.class);
    }

    @Test
    @DisplayName("Casting an instant that targets a creature adds a counter to each creature you control")
    void reparteeBuffsAllCreatures() {
        harness.addToBattlefield(player1, new StirringHopesinger());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hopesinger = findPermanent(player1, "Stirring Hopesinger");
        Permanent ownGiant = findPermanent(player1, "Hill Giant");
        Permanent oppGiant = findPermanent(player2, "Hill Giant");
        assertThat(hopesinger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Opponent's creature is unaffected — "you control" only
        assertThat(oppGiant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new StirringHopesinger());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }
}
