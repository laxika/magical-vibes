package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.l.LightningProwess;
import com.github.laxika.magicalvibes.cards.m.MistRaven;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TandemLookout.class, LightningProwess.class,
        MoorlandInquisitor.class, MistRaven.class})
class TandemLookoutTest extends BaseCardTest {

    private Permanent pairWithPartner() {
        Permanent bears = addCreatureReady(player1, new MoorlandInquisitor());
        harness.castFromHand(player1, new TandemLookout(), "{2}{U}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private int handSize(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerHands.get(player.getId()).size();
    }

    @Test
    @DisplayName("Paired partner dealing combat damage to an opponent draws a card")
    void pairedPartnerDrawsOnCombatDamage() {
        pairWithPartner();
        harness.setHand(player1, List.of());
        int before = handSize(player1);

        declareAttackers(List.of(0)); // Moorland Inquisitor attacks
        resolveCombat();
        harness.passBothPriorities(); // resolve the granted draw trigger

        assertThat(handSize(player1)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Unpaired Tandem Lookout grants no draw trigger")
    void unpairedGrantsNothing() {
        addCreatureReady(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new TandemLookout());
        harness.setHand(player1, List.of());
        int before = handSize(player1);

        declareAttackers(List.of(0)); // Moorland Inquisitor attacks
        resolveCombat();
        harness.passBothPriorities();

        assertThat(handSize(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("Tandem Lookout itself draws when it deals combat damage to an opponent")
    void pairedLookoutDrawsOnCombatDamage() {
        pairWithPartner();
        Permanent lookout = findPermanent(player1, "Tandem Lookout");
        lookout.setSummoningSick(false);
        harness.setHand(player1, List.of());
        int before = handSize(player1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(lookout);
        declareAttackers(List.of(index));
        resolveCombat();
        harness.passBothPriorities(); // resolve the granted draw trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(handSize(player1)).isEqualTo(before + 1);
    }

    private void grantPing(Permanent creature) {
        harness.setHand(player1, List.of(new LightningProwess()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paired partner draws for noncombat damage to an opponent")
    void pairedPartnerDrawsOnNoncombatDamage() {
        Permanent partner = pairWithPartner();
        grantPing(partner);
        harness.setHand(player1, List.of());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(partner);
        harness.activateAbility(player1, index, 1, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(handSize(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paired Lookout draws for noncombat damage to an opponent")
    void pairedLookoutDrawsOnNoncombatDamage() {
        pairWithPartner();
        Permanent lookout = findPermanent(player1, "Tandem Lookout");
        grantPing(lookout);
        harness.setHand(player1, List.of());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(lookout);
        harness.activateAbility(player1, index, null, player2.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(handSize(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage to the paired creature's controller does not draw")
    void damageToControllerDoesNotDraw() {
        Permanent partner = pairWithPartner();
        grantPing(partner);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(handSize(player1)).isZero();
    }

    @Test
    @DisplayName("Soulbond does not trigger when Lookout enters without a partner")
    void enteringWithoutPartnerDoesNotTrigger() {
        harness.castFromHand(player1, new TandemLookout(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature is not an eligible soulbond partner")
    void opponentsCreatureDoesNotEnableSoulbond() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.castFromHand(player1, new TandemLookout(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Soulbond may pair with a creature entering after Lookout")
    void pairsWithLaterEnteringCreature() {
        harness.addToBattlefield(player1, new TandemLookout());
        harness.castFromHand(player1, new MoorlandInquisitor(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent partner = findPermanent(player1, "Moorland Inquisitor");
        partner.setSummoningSick(false);
        harness.setHand(player1, List.of());
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(handSize(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining soulbond leaves Lookout without its draw ability")
    void mayDeclinePairing() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.castFromHand(player1, new TandemLookout(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        findPermanent(player1, "Tandem Lookout").setSummoningSick(false);
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(handSize(player1)).isZero();
    }

    @Test
    @DisplayName("Partner loses the draw ability when Lookout leaves the battlefield")
    void partnerStopsDrawingAfterLookoutLeaves() {
        pairWithPartner();
        Permanent lookout = findPermanent(player1, "Tandem Lookout");
        harness.setHand(player1, List.of(new MistRaven()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, lookout.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Tandem Lookout");
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(handSize(player1)).isZero();
    }
}
