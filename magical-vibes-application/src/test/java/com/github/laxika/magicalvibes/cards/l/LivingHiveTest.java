package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.p.PlatedSlagwurm;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LivingHive.class, Frogmite.class, PlatedSlagwurm.class})
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
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().isToken()).isTrue();
        });
    }

    @Test
    @DisplayName("Trample damage to a player creates tokens equal to the damage that trampled over")
    void createsTokensFromTrampleDamage() {
        Permanent hive = addCreatureReady(player1, new LivingHive());
        hive.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Frogmite());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(findPermanents(player1, "Insect")).hasSize(4);
        harness.assertInGraveyard(player2, "Frogmite");
    }

    @Test
    @DisplayName("No tokens are created when all combat damage is assigned to a blocker")
    void doesNotTriggerWithoutCombatDamageToPlayer() {
        Permanent hive = addCreatureReady(player1, new LivingHive());
        hive.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PlatedSlagwurm());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Insect")).isEmpty();
        harness.assertOnBattlefield(player2, "Plated Slagwurm");
    }
}
