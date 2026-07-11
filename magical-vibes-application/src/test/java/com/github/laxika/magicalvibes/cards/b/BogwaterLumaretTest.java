package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.amount.Fixed;

class BogwaterLumaretTest extends BaseCardTest {

    

    @Test
    @DisplayName("Entering the battlefield triggers self life gain")
    void selfEntryTriggersLifeGain() {
        harness.setHand(player1, List.of(new BogwaterLumaret()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Bogwater Lumaret");
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) trigger.getEffectsToResolve().getFirst()).amount()).isEqualTo(new Fixed(1));
    }

    @Test
    @DisplayName("Resolving self-ETB trigger gains 1 life")
    void selfEntryGainsOneLife() {
        harness.setHand(player1, List.of(new BogwaterLumaret()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Another creature entering triggers life gain")
    void anotherCreatureEnteringTriggersLifeGain() {
        harness.addToBattlefield(player1, new BogwaterLumaret());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Bogwater Lumaret");
        assertThat(trigger.getEffectsToResolve().getFirst()).isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) trigger.getEffectsToResolve().getFirst()).amount()).isEqualTo(new Fixed(1));
    }

    @Test
    @DisplayName("Another creature entering resolves and gains 1 life")
    void anotherCreatureGainsOneLife() {
        harness.addToBattlefield(player1, new BogwaterLumaret());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not trigger for opponent's creatures")
    void doesNotTriggerForOpponentCreatures() {
        harness.addToBattlefield(player2, new BogwaterLumaret());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two Bogwater Lumarets trigger separately for a creature entering")
    void twoLumaretsTriggerSeparately() {
        harness.addToBattlefield(player1, new BogwaterLumaret());
        harness.addToBattlefield(player1, new BogwaterLumaret());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }
}
