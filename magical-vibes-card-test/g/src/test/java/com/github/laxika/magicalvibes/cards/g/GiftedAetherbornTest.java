package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FoundryAssembler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiftedAetherborn.class, FoundryAssembler.class})
class GiftedAetherbornTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent aetherborn = addCreatureReady(player1, new GiftedAetherborn());
        Permanent blocker = addCreatureReady(player2, new FoundryAssembler());

        declareAttackersAndPrepareBlockers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aetherborn)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(aetherborn))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aetherborn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent aetherborn = addCreatureReady(player1, new GiftedAetherborn());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(aetherborn)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Blocking Aetherborn gains life and kills its attacker even when both die")
    void blockingAetherbornGainsLifeWhenBothCreaturesDie() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new FoundryAssembler());
        Permanent aetherborn = addCreatureReady(player2, new GiftedAetherborn());

        declareAttackersAndPrepareBlockers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(aetherborn),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(aetherborn);
        harness.assertInGraveyard(player1, "Foundry Assembler");
        harness.assertInGraveyard(player2, "Gifted Aetherborn");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }
}
