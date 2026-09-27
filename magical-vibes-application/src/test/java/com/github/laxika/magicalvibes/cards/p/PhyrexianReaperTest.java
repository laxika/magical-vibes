package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.cards.z.ZanamDjinn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianReaper.class, GiantSpider.class, WallOfAir.class, PincerSpider.class, ZanamDjinn.class})
class PhyrexianReaperTest extends BaseCardTest {

    @Test
    @DisplayName("When Phyrexian Reaper becomes blocked by a green creature, it destroys that creature without regeneration")
    void becomesBlockedByGreenCreatureDestroysItWithoutRegeneration() {
        Permanent reaper = addReadyReaper(player1);
        reaper.setAttacking(true);
        Permanent spider = addReadySpider(player2);
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
        Permanent reaper = addReadyReaper(player1);
        reaper.setAttacking(true);
        addReadyWall(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Phyrexian Reaper"));

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Air");
    }

    private Permanent addReadyReaper(Player player) {
        Permanent perm = new Permanent(new PhyrexianReaper());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new WallOfAir());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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
}
