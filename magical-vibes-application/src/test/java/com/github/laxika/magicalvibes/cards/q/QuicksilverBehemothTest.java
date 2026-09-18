package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GreaterHarvester;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuicksilverBehemoth.class, DarksteelCitadel.class, CrazedGoblin.class, GreaterHarvester.class})
class QuicksilverBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new DarksteelCitadel());
        }
        harness.setHand(player1, List.of(new QuicksilverBehemoth()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only artifacts controlled by the spell's controller")
    void affinityCountsOnlyControlledArtifacts() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new DarksteelCitadel());
        }
        harness.setHand(player1, List.of(new QuicksilverBehemoth()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Attacking schedules Quicksilver Behemoth to return at end of combat")
    void attackingSchedulesReturnToHand() {
        addCreatureReady(player1, new QuicksilverBehemoth());

        declareAttackers(List.of(0));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Quicksilver Behemoth");
    }

    @Test
    @DisplayName("Blocking schedules Quicksilver Behemoth to return at end of combat")
    void blockingSchedulesReturnToHand() {
        addCreatureReady(player1, new CrazedGoblin());
        Permanent behemoth = addCreatureReady(player2, new QuicksilverBehemoth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(action -> action.permanentId().equals(behemoth.getId()));

        harness.passBothPriorities();

        harness.assertInHand(player2, "Quicksilver Behemoth");
    }

    @Test
    @DisplayName("Quicksilver Behemoth is not returned if it dies in combat")
    void notReturnedIfItDiesInCombat() {
        addCreatureReady(player1, new GreaterHarvester());
        addCreatureReady(player2, new QuicksilverBehemoth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Quicksilver Behemoth");
        harness.assertNotInHand(player2, "Quicksilver Behemoth");
    }

    @Test
    @DisplayName("Quicksilver Behemoth is not returned if it leaves before end of combat")
    void notReturnedIfItLeavesBeforeEndOfCombat() {
        Permanent behemoth = addCreatureReady(player1, new QuicksilverBehemoth());

        declareAttackers(List.of(0));

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(behemoth.getId()));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Quicksilver Behemoth");
    }
}
