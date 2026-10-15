package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SpittingEarth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RehearsedDebater.class, HillGiant.class, Shock.class, SpittingEarth.class})
class RehearsedDebaterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature gives +1/+1 until end of turn")
    void reparteeBoostsSelf() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Rehearsed Debater"));

        resolveAllTriggers();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(4);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
    }

    @Test
    @DisplayName("Repartee boost wears off at cleanup")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(3);
    }

    @Test
    @DisplayName("A sorcery targeting an opponent's creature triggers Repartee")
    void sorceryTargetingOpponentsCreatureTriggers() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SpittingEarth()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's instant targeting a creature does not trigger Repartee")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Hill Giant"));
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(3);
    }

    @Test
    @DisplayName("Repartee can trigger from targeting itself and resolves before the spell")
    void selfTargetingSpellBoostsBeforeResolving() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID debaterId = harness.getPermanentId(player1, "Rehearsed Debater");

        harness.castInstant(player1, 0, debaterId);
        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(3);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(4);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Rehearsed Debater");
    }

    @Test
    @DisplayName("Each qualifying spell adds another boost in the same turn")
    void multipleSpellsGiveCumulativeBoosts() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Hill Giant"));
        resolveAllTriggers();
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        resolveAllTriggers();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(5);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Repartee")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new RehearsedDebater());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent debater = findPermanent(player1, "Rehearsed Debater");
        assertThat(gqs.getEffectivePower(gd, debater)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, debater)).isEqualTo(3);
    }
}
