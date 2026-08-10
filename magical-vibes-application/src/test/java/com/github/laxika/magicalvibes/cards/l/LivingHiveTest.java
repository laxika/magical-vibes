package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LivingHiveTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player creates that many Insect tokens")
    void createsTokensEqualToCombatDamage() {
        Permanent hive = addCreatureReady(player1, new LivingHive());
        hive.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        List<Permanent> tokens = findPermanents(player1, "Insect");
        assertThat(tokens).hasSize(6);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Trample damage to a player creates tokens equal to the damage that trampled over")
    void createsTokensFromTrampleDamage() {
        Permanent hive = addCreatureReady(player1, new LivingHive());
        hive.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        advanceToCombatDamageAssignment();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Insect")).hasSize(4);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No tokens are created when all combat damage is assigned to a blocker")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent hive = addCreatureReady(player1, new LivingHive());
        hive.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, creature("Great Wall", 0, 6));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        advanceToCombatDamageAssignment();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Insect")).isEmpty();
    }

    private static Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private void advanceToCombatDamageAssignment() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
