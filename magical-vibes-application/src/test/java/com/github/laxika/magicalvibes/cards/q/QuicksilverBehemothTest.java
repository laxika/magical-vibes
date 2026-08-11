package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuicksilverBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Spellbook());
        }
        harness.setHand(player1, List.of(new QuicksilverBehemoth()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Attacking schedules Quicksilver Behemoth to return at end of combat")
    void attackingSchedulesReturnToHand() {
        Permanent behemoth = addReady(player1, new QuicksilverBehemoth());

        declareAttackers(List.of(0));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Quicksilver Behemoth");
    }

    @Test
    @DisplayName("Blocking schedules Quicksilver Behemoth to return at end of combat")
    void blockingSchedulesReturnToHand() {
        Permanent attacker = addReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent behemoth = addReady(player2, new QuicksilverBehemoth());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(behemoth.getId()));

        harness.passBothPriorities();

        harness.assertInHand(player2, "Quicksilver Behemoth");
    }

    @Test
    @DisplayName("Quicksilver Behemoth is not returned if it leaves before end of combat")
    void notReturnedIfItLeavesBeforeEndOfCombat() {
        Permanent behemoth = addReady(player1, new QuicksilverBehemoth());

        declareAttackers(List.of(0));

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(behemoth.getId()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Quicksilver Behemoth");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
