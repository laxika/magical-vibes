package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.s.SamiteArcher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArmadilloCloak.class, NomadicElf.class, SamiteArcher.class})
class ArmadilloCloakTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and trample")
    void enchantedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1, new NomadicElf());
        attachCloak(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Aura controller gains life from combat damage, including when the creature dies")
    void auraControllerGainsLifeFromCombatDamageWhenCreatureDies() {
        NomadicElf card = new NomadicElf();
        card.setPower(1);
        Permanent creature = addCreatureReady(player1, card);
        creature.setAttacking(true);
        attachCloak(player1, creature);

        NomadicElf blockerCard = new NomadicElf();
        blockerCard.setPower(8);
        blockerCard.setToughness(8);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 10);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 3));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        harness.assertInGraveyard(player1, "Armadillo Cloak");
    }

    @Test
    @DisplayName("Aura controller gains life from noncombat damage to a player")
    void auraControllerGainsLifeFromNoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new SamiteArcher());
        attachCloak(player2, creature);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    private void attachCloak(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ArmadilloCloak());
        aura.setAttachedTo(creature.getId());
    }
}
