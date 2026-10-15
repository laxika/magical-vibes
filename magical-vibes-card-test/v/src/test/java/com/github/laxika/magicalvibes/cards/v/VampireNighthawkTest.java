package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SentinelSpider;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampireNighthawk.class, WalkingCorpse.class, WindDrake.class, SentinelSpider.class})
class VampireNighthawkTest extends BaseCardTest {

    @Test
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new VampireNighthawk());
        addCreatureReady(player2, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockAndLifelinkGainsLifeFromCreatureDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VampireNighthawk());
        Permanent blocker = addCreatureReady(player2, new WindDrake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Wind Drake");
        harness.assertOnBattlefield(player1, "Vampire Nighthawk");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void reachBlockerDiesToDeathtouchAndLifelinkStillWorksWhenNighthawkDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VampireNighthawk());
        addCreatureReady(player2, new SentinelSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Vampire Nighthawk");
        harness.assertNotOnBattlefield(player2, "Sentinel Spider");
        harness.assertInGraveyard(player1, "Vampire Nighthawk");
        harness.assertInGraveyard(player2, "Sentinel Spider");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void unblockedCombatDamageGainsLifeForItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent nighthawk = addCreatureReady(player2, new VampireNighthawk());
        nighthawk.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    void blockingGroundCreatureDealsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new VampireNighthawk());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Vampire Nighthawk");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 22);
    }
}
