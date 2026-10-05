package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.cards.z.ZanamDjinn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.l.LlanowarKnight;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianReaper.class, GiantSpider.class, WallOfAir.class, PincerSpider.class, ZanamDjinn.class, LlanowarKnight.class})
class PhyrexianReaperTest extends BaseCardTest {

    @Test
    @DisplayName("When Phyrexian Reaper becomes blocked by a green creature, it destroys that creature without regeneration")
    void becomesBlockedByGreenCreatureDestroysItWithoutRegeneration() {
        Permanent reaper = addCreatureReady(player1, new PhyrexianReaper());
        reaper.setAttacking(true);
        Permanent spider = addCreatureReady(player2, new GiantSpider());
        spider.setRegenerationShield(1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Phyrexian Reaper")
                        && se.getTargetId().equals(spider.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
        assertThat(spider.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("When Phyrexian Reaper becomes blocked by a non-green creature, it does not destroy that creature")
    void becomesBlockedByNonGreenCreatureDoesNotDestroyIt() {
        Permanent reaper = addCreatureReady(player1, new PhyrexianReaper());
        reaper.setAttacking(true);
        addCreatureReady(player2, new WallOfAir());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Phyrexian Reaper"));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Air");
    }

    @Test
    @DisplayName("When Phyrexian Reaper is blocked by multiple creatures, it destroys each green blocker and leaves the non-green blocker")
    void destroysEachGreenBlockerAmongMultipleBlockers() {
        Permanent reaper = addCreatureReady(player1, new PhyrexianReaper());
        reaper.setAttacking(true);
        Permanent firstGreenBlocker = addCreatureReady(player2, new PincerSpider());
        firstGreenBlocker.setRegenerationShield(1);
        Permanent secondGreenBlocker = addCreatureReady(player2, new PincerSpider());
        secondGreenBlocker.setRegenerationShield(1);
        Permanent nonGreenBlocker = addCreatureReady(player2, new ZanamDjinn());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.stack).filteredOn(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Phyrexian Reaper"))
                .hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonGreenBlocker);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Pincer Spider"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Pincer Spider"))
                .hasSize(2);
        assertThat(firstGreenBlocker.getRegenerationShield()).isEqualTo(1);
        assertThat(secondGreenBlocker.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A multicolored green blocker is destroyed despite protection from black")
    void destroysGreenBlockerWithProtectionFromBlack() {
        Permanent reaper = addCreatureReady(player1, new PhyrexianReaper());
        reaper.setAttacking(true);
        addCreatureReady(player2, new LlanowarKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Llanowar Knight");
        harness.assertInGraveyard(player2, "Llanowar Knight");
    }

    @Test
    @DisplayName("The destruction trigger resolves after Phyrexian Reaper leaves the battlefield")
    void triggerResolvesAfterReaperLeavesBattlefield() {
        Permanent reaper = addCreatureReady(player1, new PhyrexianReaper());
        reaper.setAttacking(true);
        addCreatureReady(player2, new PincerSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(reaper);
        gd.playerGraveyards.get(player1.getId()).add(reaper.getCard());

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Pincer Spider");
        harness.assertInGraveyard(player2, "Pincer Spider");
    }
}
