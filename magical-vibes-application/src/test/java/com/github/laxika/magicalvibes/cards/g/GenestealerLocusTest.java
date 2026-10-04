package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WildJhovall;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenestealerLocus.class, WildJhovall.class})
class GenestealerLocusTest extends BaseCardTest {

    @Test
    @DisplayName("A creature attacking the Locus controller gets -1/-0")
    void weakensCreatureAttackingController() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player2, new WildJhovall());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature attacking an opponent of the Locus controller gets +0/+1")
    void strengthensCreatureAttackingOpponent() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player1, new WildJhovall());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attacking the Locus controller does not receive the opponent bonus")
    void doesNotStrengthenCreatureAttackingController() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player2, new WildJhovall());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking an opponent does not receive the controller debuff")
    void doesNotWeakenCreatureAttackingOpponent() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player1, new WildJhovall());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
    }

    @Test
    @DisplayName("Genestealer Locus receives its own attacking-opponent bonus")
    void boostsItselfWhenAttackingOpponent() {
        Permanent locus = addCreatureReady(player1, new GenestealerLocus());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, locus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, locus)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each creature attacking the controller is weakened independently")
    void weakensEveryAttackerButNotNonattackingCreatures() {
        addCreatureReady(player1, new GenestealerLocus());
        Permanent first = addCreatureReady(player2, new WildJhovall());
        Permanent second = addCreatureReady(player2, new WildJhovall());
        Permanent nonattacker = addCreatureReady(player2, new WildJhovall());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nonattacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple Locus copies each boost every creature attacking their opponent")
    void multipleCopiesBoostEveryAttacker() {
        Permanent first = addCreatureReady(player1, new GenestealerLocus());
        Permanent second = addCreatureReady(player1, new GenestealerLocus());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
    }

    @Test
    @DisplayName("The controller's attack trigger still resolves after Locus leaves")
    void debuffResolvesAfterSourceLeavesBattlefield() {
        Permanent locus = addCreatureReady(player1, new GenestealerLocus());
        Permanent attacker = addCreatureReady(player2, new WildJhovall());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(locus);
        gd.playerGraveyards.get(player1.getId()).add(locus.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opposing Locus abilities both apply and expire at cleanup")
    void bothModifiersExpireAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GenestealerLocus());
        addCreatureReady(player2, new GenestealerLocus()).tap();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }
}
