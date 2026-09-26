package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GaleForce;
import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.y.YoseiTheMorningStar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CandlesGlow.class, GaleForce.class, GlacialRay.class, YoseiTheMorningStar.class})
class CandlesGlowTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the damage actually prevented from a player")
    void gainsLifeEqualToDamageActuallyPrevented() {
        harness.setLife(player1, 20);
        resolveCandlesGlow(player1.getId());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Works for a permanent target and lets excess damage through")
    void worksForPermanentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YoseiTheMorningStar());
        harness.setLife(player1, 20);
        resolveCandlesGlow(target.getId());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GaleForce()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves Candles' Glow in hand")
    void splicesOntoArcaneSpell() {
        GlacialRay arcaneRay = new GlacialRay();
        CandlesGlow candlesGlow = new CandlesGlow();
        harness.setHand(player1, List.of(arcaneRay, candlesGlow));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(candlesGlow);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void preventionShieldExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        resolveCandlesGlow(player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new GlacialRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private void resolveCandlesGlow(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CandlesGlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
