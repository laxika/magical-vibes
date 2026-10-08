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

        fish.setAttacking(true);
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

        fish.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fish)))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent fish = addCreatureReady(player1, new VividFlyingFish());

        fish.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(fish)))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }
    @Test
    @DisplayName("Blocking does not grant flying")
    void cannotBlockFlyingCreatureWhileNotAttacking() {
        Permanent attacker = addCreatureReady(player1, new SuntailHawk());
        Permanent fish = addCreatureReady(player2, new VividFlyingFish());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gqs.hasKeyword(gd, fish, Keyword.FLYING)).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(fish),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Only the attacking Fish gains flying")
    void flyingAppliesOnlyToTheAttackingFish() {
        Permanent attacker = addCreatureReady(player1, new VividFlyingFish());
        Permanent idleFish = addCreatureReady(player1, new VividFlyingFish());
        addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, idleFish, Keyword.FLYING)).isFalse();
    }
}
