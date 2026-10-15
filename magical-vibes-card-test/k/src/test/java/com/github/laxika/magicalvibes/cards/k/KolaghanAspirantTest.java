package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AtarkaBeastbreaker;
import com.github.laxika.magicalvibes.cards.d.DragonlordOjutai;
import com.github.laxika.magicalvibes.cards.t.TwinBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KolaghanAspirant.class, AtarkaBeastbreaker.class, TwinBolt.class, DragonlordOjutai.class})
class KolaghanAspirantTest extends BaseCardTest {

    @Test
    @DisplayName("When Kolaghan Aspirant becomes blocked, it deals 1 damage to the blocker")
    void becomingBlockedDamagesBlocker() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AtarkaBeastbreaker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Kolaghan Aspirant becomes blocked by two creatures, it deals 1 damage to each blocker")
    void becomingBlockedDamagesEachBlocker() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new AtarkaBeastbreaker());
        Permanent secondBlocker = addCreatureReady(player2, new AtarkaBeastbreaker());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void damagesUntappedHexproofBlockerWithoutTargeting() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DragonlordOjutai());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.isTapped()).isFalse();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void killsOneToughnessBlockerBeforeCombatDamage() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KolaghanAspirant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aspirant);
        assertThat(aspirant.getMarkedDamage()).isZero();
    }

    @Test
    void triggerStillDealsDamageAfterAspirantDies() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AtarkaBeastbreaker());
        harness.setHand(player2, List.of(new TwinBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.castInstant(player2, 0, Map.of(aspirant.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aspirant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aspirant.getCard());
        assertThat(blocker.getMarkedDamage()).isZero();

        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void removingOneBlockerDoesNotPreventDamageToTheOther() {
        Permanent aspirant = addCreatureReady(player1, new KolaghanAspirant());
        aspirant.setAttacking(true);
        Permanent firstBlocker = addCreatureReady(player2, new AtarkaBeastbreaker());
        Permanent secondBlocker = addCreatureReady(player2, new AtarkaBeastbreaker());
        harness.setHand(player1, List.of(new TwinBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.castInstant(player1, 0, Map.of(firstBlocker.getId(), 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(firstBlocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(firstBlocker.getCard());

        resolveAllTriggers();

        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(1);
    }

}
