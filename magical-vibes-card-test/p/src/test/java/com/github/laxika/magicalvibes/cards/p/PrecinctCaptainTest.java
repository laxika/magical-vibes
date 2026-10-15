package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrecinctCaptain.class})
class PrecinctCaptainTest extends BaseCardTest {

    private long soldierCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Soldier".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("Creates a 1/1 white Soldier token when dealing combat damage to a player")
    void createsSoldierOnCombatDamage() {
        Permanent captain = addCreatureReady(player1, new PrecinctCaptain());
        captain.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities(); // resolve the trigger

        assertThat(soldierCount()).isEqualTo(1);
        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no token when it deals no combat damage to a player")
    void noTokenWithoutCombatDamageToPlayer() {
        addCreatureReady(player1, new PrecinctCaptain());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(soldierCount()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature creates no Soldier")
    void noTokenWhenBlockedByAnotherCaptain() {
        addCreatureReady(player1, new PrecinctCaptain());
        harness.addToBattlefield(player2, new PrecinctCaptain());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Precinct Captain");
        harness.assertInGraveyard(player2, "Precinct Captain");
        assertThat(soldierCount()).isZero();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Each Captain that damages a player creates its own Soldier")
    void twoCaptainsCreateTwoSoldiers() {
        addCreatureReady(player1, new PrecinctCaptain()).setAttacking(true);
        addCreatureReady(player1, new PrecinctCaptain()).setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 16);
        assertThat(soldierCount()).isZero();
        resolveAllTriggers();
        assertThat(soldierCount()).isEqualTo(2);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }
}
