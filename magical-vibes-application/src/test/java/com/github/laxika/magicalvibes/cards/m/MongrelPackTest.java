package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HeartwoodTreefolk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MongrelPack.class, HeartwoodTreefolk.class})
class MongrelPackTest extends BaseCardTest {

    @Test
    @DisplayName("Dying in combat creates four 1/1 Dog tokens")
    void diesInCombatCreatesDogs() {
        Permanent pack = addCreatureReady(player1, new MongrelPack());
        harness.addToBattlefield(player2, new HeartwoodTreefolk()); // 3/4 kills the 4/1 Pack

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mongrel Pack");
        assertThat(countPermanents(player1, "Dog")).isEqualTo(4);
        assertThat(findPermanents(player1, "Dog"))
                .allSatisfy(dog -> {
                    assertThat(dog.getCard().getPower()).isEqualTo(1);
                    assertThat(dog.getCard().getToughness()).isEqualTo(1);
                });
    }

    @Test
    @DisplayName("Dying during combat creates Dogs even when it was not attacking or blocking")
    void diesDuringCombatWithoutAttackingOrBlockingCreatesDogs() {
        Permanent pack = addCreatureReady(player1, new MongrelPack());

        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        pack.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mongrel Pack");
        assertThat(countPermanents(player1, "Dog")).isEqualTo(4);
    }

    @Test
    @DisplayName("Dying outside combat creates no tokens")
    void diesOutsideCombatCreatesNoDogs() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new MongrelPack());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        pack.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Mongrel Pack");
        assertThat(countPermanents(player1, "Dog")).isZero();
    }
}
