package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HeartwoodTreefolk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MongrelPack.class, HeartwoodTreefolk.class})
class MongrelPackTest extends BaseCardTest {

    @Test
    @DisplayName("Dying in combat creates four 1/1 Dog tokens")
    void diesInCombatCreatesDogs() {
        addCreatureReady(player1, new MongrelPack());
        harness.addToBattlefield(player2, new HeartwoodTreefolk()); // 3/4 kills the 4/1 Pack

        declareAttackersAndPrepareBlockers(List.of(0));
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

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {
            "DECLARE_ATTACKERS", "DECLARE_BLOCKERS", "COMBAT_DAMAGE", "END_OF_COMBAT"
    })
    @DisplayName("Death in any remaining combat step creates Dogs for the defending controller")
    void defendingControllersPackDiesDuringCombat(TurnStep step) {
        Permanent pack = harness.addToBattlefieldAndReturn(player2, new MongrelPack());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
        harness.clearPriorityPassed();

        pack.setMarkedDamage(1);
        harness.runStateBasedActions();
        assertThat(countPermanents(player2, "Dog")).isZero();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Mongrel Pack");
        assertThat(countPermanents(player1, "Dog")).isZero();
        assertThat(findPermanents(player2, "Dog")).hasSize(4).allSatisfy(dog -> {
            assertThat(dog.getCard().isToken()).isTrue();
            assertThat(dog.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(dog.getCard().getSubtypes()).containsExactly(CardSubtype.DOG);
            assertThat(dog.getCard().getPower()).isEqualTo(1);
            assertThat(dog.getCard().getToughness()).isEqualTo(1);
            assertThat(dog.isTapped()).isFalse();
            assertThat(dog.isAttacking()).isFalse();
        });
    }

    @Test
    @DisplayName("Death after combat creates no Dogs")
    void diesAfterCombatCreatesNoDogs() {
        Permanent pack = harness.addToBattlefieldAndReturn(player1, new MongrelPack());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        pack.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mongrel Pack");
        assertThat(countPermanents(player1, "Dog")).isZero();
    }
}
