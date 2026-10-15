package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NiblisOfDusk.class, Shock.class, GrizzlyBears.class})
class NiblisOfDuskTest extends BaseCardTest {

    private Permanent addNiblis() {
        Permanent niblis = harness.addToBattlefieldAndReturn(player1, new NiblisOfDusk());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return niblis;
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent niblis = addNiblis();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isEqualTo(1);

        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent niblis = addNiblis();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess: the boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent niblis = addNiblis();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess resolves before the spell that triggered it")
    void prowessResolvesBeforeSpell() {
        Permanent niblis = addNiblis();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Multiple noncreature spells give cumulative prowess boosts")
    void multipleSpellsGiveCumulativeBoosts() {
        Permanent niblis = addNiblis();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotTriggerProwess() {
        Permanent niblis = addNiblis();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, niblis)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, niblis)).isEqualTo(1);
        harness.assertLife(player1, 18);
    }
}
