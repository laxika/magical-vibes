package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KaijinOfTheVanishingTouchTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature schedules that attacker for an end-of-combat bounce")
    void blockingSchedulesReturnToHand() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Kaijin of the Vanishing Touch")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("The blocked attacker is returned to its owner's hand at end of combat")
    void blockedAttackerReturnedToHand() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInHand(player1, "Giant Spider");
    }

    @Test
    @DisplayName("The blocked attacker still deals its combat damage before the bounce")
    void attackerStillDealsCombatDamage() {
        Permanent attacker = addReady(player1, new HillGiant());
        attacker.setAttacking(true);
        addReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        // 3 damage kills the 0/3 Kaijin, and the attacker is still bounced afterwards.
        harness.assertInGraveyard(player2, "Kaijin of the Vanishing Touch");
        harness.assertInHand(player1, "Hill Giant");
    }

    @Test
    @DisplayName("An attacker that left the battlefield before end of combat is not returned")
    void attackerGoneBeforeEndOfCombatIsNotReturned() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addReady(player2, new KaijinOfTheVanishingTouch());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(attacker.getId()));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Giant Spider");
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
