package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RingwardenOwl.class, Shock.class, GrizzlyBears.class})
class RingwardenOwlTest extends BaseCardTest {

    private Permanent addOwl() {
        Permanent owl = harness.addToBattlefieldAndReturn(player1, new RingwardenOwl());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return owl;
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent owl = addOwl();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(4);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent owl = addOwl();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess: the boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent owl = addOwl();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve prowess trigger
        harness.passBothPriorities(); // resolve Shock

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prowess resolves before the spell that triggered it")
    void prowessResolvesBeforeSpell() {
        Permanent owl = addOwl();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(4);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each noncreature spell adds another prowess boost")
    void multipleSpellsGiveCumulativeBoosts() {
        Permanent owl = addOwl();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent owl = addOwl();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, owl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, owl)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flying prevents ground creatures from blocking but allows flying blockers")
    void flyingRestrictsBlockers() {
        Permanent owl = addOwl();
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new RingwardenOwl());
        var defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(harness.getBlockLegalityService()
                .canBlockAttacker(gd, groundBlocker, owl, defenders)).isFalse();
        assertThat(harness.getBlockLegalityService()
                .canBlockAttacker(gd, flyingBlocker, owl, defenders)).isTrue();
    }
}
