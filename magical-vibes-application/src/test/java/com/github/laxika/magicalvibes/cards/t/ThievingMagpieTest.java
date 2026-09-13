package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThievingMagpie.class, HermeticStudy.class})
class ThievingMagpieTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to an opponent draws a card")
    void combatDamageToOpponentDrawsCard() {
        prepareDrawState();
        harness.setLife(player2, 20);

        Permanent magpie = addReadyMagpie(player1);
        magpie.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A blocked Magpie deals no damage to an opponent and draws no card")
    void blockedMagpieDoesNotDraw() {
        prepareDrawState();
        harness.setLife(player2, 20);

        Permanent magpie = addReadyMagpie(player1);
        magpie.setAttacking(true);

        Permanent blocker = addReadyMagpie(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncombat damage dealt by the Magpie to an opponent draws a card")
    void noncombatDamageToOpponentDrawsCard() {
        prepareDrawState();
        harness.setLife(player2, 20);

        Permanent magpie = addReadyMagpie(player1);
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(magpie.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage dealt by the Magpie to its controller does not draw a card")
    void damageToControllerDoesNotDraw() {
        prepareDrawState();
        harness.setLife(player1, 20);

        Permanent magpie = addReadyMagpie(player1);
        Permanent study = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        study.setAttachedTo(magpie.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyMagpie(com.github.laxika.magicalvibes.model.Player player) {
        Permanent magpie = harness.addToBattlefieldAndReturn(player, new ThievingMagpie());
        magpie.setSummoningSick(false);
        return magpie;
    }

    private void prepareDrawState() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThievingMagpie()));
    }
}
