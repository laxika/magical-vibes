package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VividFlyingFish.class, GrizzlyBears.class, SuntailHawk.class, GiantSpider.class})
class VividFlyingFishTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying only while attacking")
    void hasFlyingOnlyWhileAttacking() {
        Permanent fish = addCreatureReady(player1, new VividFlyingFish());

        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();

        fish.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isTrue();

        fish.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByGroundCreature() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent fish = addCreatureReady(player1, new VividFlyingFish());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fish)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        Permanent fish = addCreatureReady(player1, new VividFlyingFish());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fish)))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent fish = addCreatureReady(player1, new VividFlyingFish());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fish)))))
                .doesNotThrowAnyException();
    }
}
