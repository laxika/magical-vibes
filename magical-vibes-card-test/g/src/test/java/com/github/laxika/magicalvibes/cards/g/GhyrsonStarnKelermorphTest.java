package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.d.Diminish;
import com.github.laxika.magicalvibes.cards.f.FieryConfluence;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.v.Venomthrope;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhyrsonStarnKelermorph.class, ZuranSpellcaster.class, BalduvianBarbarians.class,
        LightningBolt.class, RagingGoblin.class, FieryConfluence.class, Diminish.class,
        Fog.class, Venomthrope.class})
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

    @Test
    void doesNotTriggerForAnOpponentsDamageSource() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player2, new ZuranSpellcaster());
        harness.setLife(player1, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersWhenYourSourceDamagesYou() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void triggersWhenYourSourceDamagesYourOwnPermanent() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player1, new BalduvianBarbarians());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Balduvian Barbarians");
    }

    @Test
    void triggersSeparatelyForSimultaneousCombatDamageFromTwoSources() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new RagingGoblin()).setAttacking(true);
        addCreatureReady(player1, new RagingGoblin()).setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.assertLife(player2, 18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
    }

    @Test
    void queuedDamageStillResolvesAfterGhyrsonLeavesTheBattlefield() {
        Permanent ghyrson = addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new ZuranSpellcaster());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, ghyrson.getId());
        harness.assertInGraveyard(player1, "Ghyrson Starn, Kelermorph");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void doesNotDealFollowUpDamageToARecipientThatAlreadyDied() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new ZuranSpellcaster());
        Permanent target = addCreatureReady(player2, new RagingGoblin());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Raging Goblin");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void wardCountersAnOpponentsSpellWhenTheyCannotPay() {
        Permanent ghyrson = addCreatureReady(player1, new GhyrsonStarnKelermorph());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ghyrson.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghyrson Starn, Kelermorph");
        assertThat(ghyrson.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersAnOpponentsActivatedAbilityWhenTheyCannotPay() {
        Permanent ghyrson = addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player2, new ZuranSpellcaster());

        harness.activateAbility(player2, 0, null, ghyrson.getId());
        harness.passBothPriorities();

        assertThat(ghyrson.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingWardAllowsAnOpponentsSpellToResolve() {
        Permanent ghyrson = addCreatureReady(player1, new GhyrsonStarnKelermorph());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, ghyrson.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghyrson Starn, Kelermorph");
    }

    @Test
    void triggersEvenWhenGhyrsonDiesInTheSameCombatDamageStep() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player1, new RagingGoblin());
        addCreatureReady(player2, new BalduvianBarbarians());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.assertInGraveyard(player1, "Ghyrson Starn, Kelermorph");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void repeatedOneDamageEventsWithinOneSpellEachTriggerForEveryRecipient() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player2, new BalduvianBarbarians());
        harness.setHand(player1, List.of(new FieryConfluence()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorceryWithModes(player1, 0, 3, 0, 0, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghyrson Starn, Kelermorph");
        harness.assertInGraveyard(player2, "Balduvian Barbarians");
        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerForItsOwnOneCombatDamage() {
        Permanent ghyrson = addCreatureReady(player1, new GhyrsonStarnKelermorph());
        harness.setHand(player1, List.of(new Diminish()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, ghyrson.getId());
        ghyrson.setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void preventedCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        Permanent attacker = addCreatureReady(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0);
        attacker.setAttacking(true);
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void followUpDamageDoesNotTargetAHexproofRecipient() {
        addCreatureReady(player1, new GhyrsonStarnKelermorph());
        addCreatureReady(player2, new Venomthrope());
        harness.setHand(player1, List.of(new FieryConfluence()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorceryWithModes(player1, 0, 3, 0, 1, 1);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Venomthrope");
        harness.assertInGraveyard(player1, "Ghyrson Starn, Kelermorph");
        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }
}
