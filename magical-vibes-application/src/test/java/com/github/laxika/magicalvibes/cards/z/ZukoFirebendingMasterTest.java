package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZukoFirebendingMaster.class, LightningBolt.class})
class ZukoFirebendingMasterTest extends BaseCardTest {

    @Test
    void firebendingAddsManaEqualToExperienceCountersUntilEndOfCombat() {
        addReadyZuko();
        gd.playerExperienceCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void castingASpellDuringCombatGivesAnExperienceCounter() {
        addReadyZuko();
        castLightningBoltDuringCombat();

        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void castingASpellOutsideCombatDoesNotGiveAnExperienceCounter() {
        addReadyZuko();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void firebendingWithNoExperienceCountersAddsNoMana() {
        addReadyZuko();

        declareAttackers(List.of(0));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void firebendingUsesExperienceCountersAtResolution() {
        addReadyZuko();
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player1, 0, player2.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void firebendingResolvesAfterZukoLeavesTheBattlefield() {
        Permanent zuko = addReadyZuko();
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.castInstant(player1, 0, zuko.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zuko);
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void castingDuringOpponentsCombatGivesAnExperienceCounter() {
        addReadyZuko();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player2.getId());
    }

    @Test
    void opponentsSpellDoesNotGiveZukosControllerExperience() {
        addReadyZuko();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerExperienceCounters).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"BEGINNING_OF_COMBAT", "END_OF_COMBAT"})
    void castingAtEitherEndOfCombatGivesExperience(TurnStep step) {
        addReadyZuko();
        harness.forceStep(step);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.withAutoStop(step, () -> {
            harness.castInstant(player1, 0, player2.getId());
            resolveAllTriggers();
            harness.castInstant(player1, 0, player2.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
    }

    private Permanent addReadyZuko() {
        return addCreatureReady(player1, new ZukoFirebendingMaster());
    }

    private void castLightningBoltDuringCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
