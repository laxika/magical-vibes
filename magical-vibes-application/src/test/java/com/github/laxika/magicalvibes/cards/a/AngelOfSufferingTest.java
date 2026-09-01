package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfSuffering.class, GrizzlyBears.class, LightningBolt.class})
class AngelOfSufferingTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage and mills twice that much")
    void preventsNoncombatDamageAndMillsTwiceThatMuch() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Prevents damage even when the library has too few cards")
    void preventsDamageWithShortLibrary() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Prevents combat damage and mills twice that much")
    void preventsCombatDamageAndMillsTwiceThatMuch() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AngelOfSuffering());
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Still mills when damage prevention is disabled")
    void stillMillsWhenDamageCannotBePrevented() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AngelOfSuffering());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        gd.damageCantBePreventedThisTurn = true;
        castLightningBoltAtPlayer1();

        harness.assertLife(player1, 17);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private void castLightningBoltAtPlayer1() {
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
    }
}
