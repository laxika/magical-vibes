package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TandemLookoutTest extends BaseCardTest {

    private Permanent pairWithBears() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TandemLookout()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castCreature(player1, 0);
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
        pairWithBears();
        harness.setHand(player1, List.of());
        int before = handSize(player1);

        declareAttackers(List.of(0)); // Grizzly Bears attacks
        resolveCombat();
        harness.passBothPriorities(); // resolve the granted draw trigger

        assertThat(handSize(player1)).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("Unpaired Tandem Lookout grants no draw trigger")
    void unpairedGrantsNothing() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TandemLookout());
        harness.setHand(player1, List.of());
        int before = handSize(player1);

        declareAttackers(List.of(0)); // Grizzly Bears attacks
        resolveCombat();
        harness.passBothPriorities();

        assertThat(handSize(player1)).isEqualTo(before);
    }

    @Test
    @DisplayName("Tandem Lookout itself draws when it deals combat damage to an opponent")
    void pairedLookoutDrawsOnCombatDamage() {
        pairWithBears();
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
}
