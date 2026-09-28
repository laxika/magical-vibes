package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhyrsonStarnKelermorph.class, ZuranSpellcaster.class, BalduvianBarbarians.class,
        LightningBolt.class, RagingGoblin.class})
class GhyrsonStarnKelermorphTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a player dealt exactly 1 damage by another source")
    void triggersForExactlyOneDamageToPlayer() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 2 damage to a permanent dealt exactly 1 damage by another source")
    void triggersForExactlyOneDamageToPermanent() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new BalduvianBarbarians());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getMarkedDamage()).isEqualTo(1);

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Balduvian Barbarians");
    }

    @Test
    @DisplayName("Does not trigger when another source deals more than 1 damage")
    void doesNotTriggerForMoreThanOneDamage() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        harness.setHand(player1, java.util.List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Triggers from exactly 1 combat damage dealt by another source")
    void triggersFromExactlyOneCombatDamage() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
