package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GoldenglowMoth;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThornwatchScarecrow.class, DevotedDruid.class, GoldenglowMoth.class, SafeholdElite.class, GreaterAuramancy.class})
class ThornwatchScarecrowTest extends BaseCardTest {

    @Test
    @DisplayName("Has wither while you control a green creature")
    void witherWithGreenCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new DevotedDruid()); // green

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("No wither without a green creature, and none from an opponent's green creature")
    void noWitherWithoutOwnGreenCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player2, new DevotedDruid()); // opponent's green

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Loses wither when the green creature leaves the battlefield")
    void losesWitherWhenGreenLeaves() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new DevotedDruid());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard() instanceof DevotedDruid);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Has vigilance while you control a white creature")
    void vigilanceWithWhiteCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new GoldenglowMoth()); // white

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("No vigilance without a white creature, and none from an opponent's white creature")
    void noVigilanceWithoutOwnWhiteCreature() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player2, new GoldenglowMoth()); // opponent's white

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A green creature grants wither but not vigilance")
    void greenGrantsWitherNotVigilance() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new DevotedDruid()); // green only

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
    }
    @Test
    void hybridCreatureGrantsBothKeywords() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new SafeholdElite());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void losesVigilanceWhenWhiteCreatureLeaves() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        Permanent moth = harness.addToBattlefieldAndReturn(player1, new GoldenglowMoth());
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(moth);

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void vigilancePreventsTappingWhenAttacking() {
        Permanent scarecrow = addCreatureReady(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new GoldenglowMoth());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(scarecrow.isTapped()).isFalse();
    }

    @Test
    void tapsWhenAttackingWithoutWhiteCreature() {
        Permanent scarecrow = addCreatureReady(player1, new ThornwatchScarecrow());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(scarecrow.isTapped()).isTrue();
    }

    @Test
    void witherDealsCombatDamageAsCounters() {
        addCreatureReady(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new DevotedDruid());
        Permanent blocker = addCreatureReady(player2, new ThornwatchScarecrow());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Thornwatch Scarecrow");
        harness.assertInGraveyard(player1, "Thornwatch Scarecrow");
    }

    @Test
    void whiteNoncreatureDoesNotGrantVigilance() {
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new GreaterAuramancy());

        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, scarecrow, Keyword.WITHER)).isFalse();
    }

    @Test
    void witherStillDealsNormalDamageToPlayers() {
        addCreatureReady(player1, new ThornwatchScarecrow());
        harness.addToBattlefield(player1, new DevotedDruid());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
    }
}
