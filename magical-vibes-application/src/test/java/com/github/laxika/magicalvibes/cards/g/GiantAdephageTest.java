package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Giant Adephage")
class GiantAdephageTest extends BaseCardTest {

    private Permanent addReadyAdephage() {
        Permanent perm = new Permanent(new GiantAdephage());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Combat damage to a player creates a token copy of this creature")
    void combatDamageCreatesTokenCopy() {
        Permanent adephage = addReadyAdephage();
        adephage.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);

        harness.passBothPriorities(); // resolve the triggered ability

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Giant Adephage"))
                .hasSize(2);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p != adephage)
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(7);
        assertThat(token.getCard().getToughness()).isEqualTo(7);
        assertThat(token.getCard().getKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("No token when the creature is blocked and deals no damage to the player")
    void noTokenWhenBlocked() {
        Permanent adephage = addReadyAdephage();
        adephage.setAttacking(true);
        harness.setLife(player2, 20);

        // 8/8 blocker soaks all 7 damage, so trample assigns nothing to the player.
        Permanent blocker = new Permanent(new AvatarOfMight());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Giant Adephage"))
                .hasSize(1);
    }
}
