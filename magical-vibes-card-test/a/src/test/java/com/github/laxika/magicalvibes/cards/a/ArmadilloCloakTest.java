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

    @Test
    @DisplayName("Noncombat damage to a creature also triggers life gain")
    void noncombatDamageToCreatureGainsLife() {
        Permanent archer = addCreatureReady(player1, new SamiteArcher());
        Permanent target = addCreatureReady(player2, new NomadicElf());
        attachCloak(player2, archer);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(11);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Each Cloak triggers independently for the same damage")
    void multipleCloaksEachGainLife() {
        Permanent archer = addCreatureReady(player1, new SamiteArcher());
        attachCloak(player1, archer);
        attachCloak(player1, archer);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Fully prevented damage does not trigger life gain")
    void preventedDamageDoesNotGainLife() {
        Permanent archer = addCreatureReady(player1, new SamiteArcher());
        addCreatureReady(player2, new SamiteArcher());
        attachCloak(player1, archer);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.activateAbility(player2, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Life gain trigger still resolves after its Aura leaves the battlefield")
    void lifeGainResolvesAfterAuraLeavesBattlefield() {
        Permanent archer = addCreatureReady(player1, new SamiteArcher());
        attachCloak(player2, archer);
        Permanent aura = findPermanent(player2, "Armadillo Cloak");
        harness.setLife(player2, 10);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerGraveyards.get(player2.getId()).add(aura.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }
    @Test
    @DisplayName("Trample damage to a blocker and player both count toward life gain")
    void trampleDamageGainsLifeForAllDamageDealt() {
        Permanent attacker = addCreatureReady(player1, new NomadicElf());
        attacker.setAttacking(true);
        attachCloak(player1, attacker);
        Permanent blocker = addCreatureReady(player2, new NomadicElf());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
        harness.assertInGraveyard(player2, "Nomadic Elf");
        harness.assertOnBattlefield(player1, "Armadillo Cloak");
    }
    private void attachCloak(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new ArmadilloCloak());
        aura.setAttachedTo(creature.getId());
    }
}
