package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LotusPathDjinn.class, GrizzlyBears.class, Shock.class})
class LotusPathDjinnTest extends BaseCardTest {

    private Permanent addDjinn() {
        Permanent djinn = harness.addToBattlefieldAndReturn(player1, new LotusPathDjinn());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return djinn;
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent djinn = addDjinn();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent djinn = addDjinn();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess: the boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent djinn = addDjinn();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(3);
    }
    @Test
    @DisplayName("Prowess resolves before its spell and repeated casts stack the boosts")
    void repeatedCastsStackBoosts() {
        Permanent djinn = addDjinn();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(4);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(5);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentsSpellDoesNotPump() {
        Permanent djinn = addDjinn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, djinn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, djinn)).isEqualTo(3);
    }
}
