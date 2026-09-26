package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.b.BlackManaBattery;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritShackle.class, DurkwoodBoars.class, BlackManaBattery.class, Boomerang.class})
class SpiritShackleTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast Spirit Shackle targeting a creature")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player1, new DurkwoodBoars());

        harness.setHand(player1, List.of(new SpiritShackle()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spirit Shackle").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot cast Spirit Shackle targeting a non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BlackManaBattery());

        harness.setHand(player1, List.of(new SpiritShackle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Attacking the enchanted creature puts a -0/-2 counter on it")
    void tappingEnchantedCreaturePutsCounter() {
        Permanent boars = addCreatureReady(player1, new DurkwoodBoars());
        attachShackle(player1, boars);

        assertThat(boars.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(boars.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, boars)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, boars)).isEqualTo(4);
    }

    @Test
    @DisplayName("An unenchanted creature becoming tapped gets no counter")
    void unenchantedCreatureGetsNoCounter() {
        Permanent boars = addCreatureReady(player1, new DurkwoodBoars());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(boars.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isZero();
    }

    @Test
    @DisplayName("The tap trigger still affects the creature if Spirit Shackle leaves before resolution")
    void tapTriggerUsesLastKnownAttachmentIfAuraLeaves() {
        Permanent boars = addCreatureReady(player1, new DurkwoodBoars());
        Permanent aura = attachShackle(player1, boars);

        declareAttackers(List.of(0));

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);

        resolveAllTriggers();

        assertThat(boars.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO)).isEqualTo(1);
    }

    private Permanent attachShackle(Player owner, Permanent creature) {
        Permanent aura = new Permanent(new SpiritShackle());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(owner.getId()).add(aura);
        return aura;
    }
}
