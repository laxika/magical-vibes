package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MesmerizingDose.class, CopperLonglegs.class, PropheticPrism.class})
class MesmerizingDoseTest extends BaseCardTest {

    @Test
    @DisplayName("Taps the enchanted creature and proliferates when it enters")
    void tapsAndProliferatesOnEnter() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent otherPermanent = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        otherPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new MesmerizingDose()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), otherPermanent.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature does not untap while Mesmerizing Dose remains attached")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        creature.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MesmerizingDose());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());

        harness.setHand(player1, List.of(new MesmerizingDose()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the enchanted creature leaves before resolution")
    void fizzlesIfTargetLeaves() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());

        harness.setHand(player1, List.of(new MesmerizingDose()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mesmerizing Dose");
        harness.assertNotOnBattlefield(player1, "Mesmerizing Dose");
    }

    @Test
    @DisplayName("Still proliferates when the enchanted creature leaves before the enter trigger resolves")
    void proliferatesAfterEnchantedCreatureLeaves() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MesmerizingDose()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mesmerizing Dose");
        assertThat(creature.isTapped()).isFalse();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(other.getId()));

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mesmerizing Dose");
    }

    @Test
    @DisplayName("May choose no objects when proliferating, even when the creature was already tapped")
    void canDeclineAllProliferationChoices() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MesmerizingDose()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Proliferates players and every existing counter kind on selected permanents")
    void proliferatesPlayersAndMultipleCounterKinds() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.OIL, 1);
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        harness.setHand(player1, List.of(new MesmerizingDose()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), player2.getId()));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(creature.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Untap restriction ends when the Aura leaves and does not affect other creatures")
    void untapRestrictionEndsWhenAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new CopperLonglegs());
        Permanent other = addCreatureReady(player2, new CopperLonglegs());
        creature.tap();
        other.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MesmerizingDose());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }
}
