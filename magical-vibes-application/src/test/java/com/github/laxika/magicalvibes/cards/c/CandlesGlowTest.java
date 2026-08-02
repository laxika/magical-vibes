package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CandlesGlowTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the damage actually prevented from a player")
    void gainsLifeEqualToDamageActuallyPrevented() {
        harness.setLife(player1, 20);
        resolveCandlesGlow(player1.getId());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Works for a permanent target and lets excess damage through")
    void worksForPermanentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        resolveCandlesGlow(target.getId());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LightningBlast()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves Candles' Glow in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        CandlesGlow candlesGlow = new CandlesGlow();
        harness.setHand(player1, List.of(arcaneShock, candlesGlow));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(candlesGlow);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    private void resolveCandlesGlow(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new CandlesGlow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
