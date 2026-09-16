package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Millikin;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Frightcrawler.class, Millikin.class, WoodlandDruid.class})
class FrightcrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 and cannot block at threshold")
    void getsThresholdBonusAndCannotBlock() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent crawler = addCreatureReady(player1, new Frightcrawler());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);

        Permanent attacker = addCreatureReady(player2, new WoodlandDruid());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, crawler), indexOf(player2, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Can block and has base stats below threshold")
    void belowThresholdHasBaseStatsAndCanBlock() {
        Permanent crawler = addCreatureReady(player1, new Frightcrawler());
        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);

        Permanent attacker = addCreatureReady(player2, new WoodlandDruid());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                indexOf(player1, crawler), indexOf(player2, attacker))));

        assertThat(crawler.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Opponent graveyard does not enable threshold")
    void opponentGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        Permanent crawler = addCreatureReady(player1, new Frightcrawler());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fear prevents nonblack nonartifact creatures from blocking")
    void fearPreventsNonblackNonartifactCreaturesFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new Frightcrawler());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new WoodlandDruid());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Fear can be blocked by black and artifact creatures")
    void fearCanBeBlockedByBlackAndArtifactCreatures() {
        Permanent attacker = addCreatureReady(player1, new Frightcrawler());
        attacker.setAttacking(true);
        Permanent blackBlocker = addCreatureReady(player2, new Frightcrawler());
        Permanent artifactBlocker = addCreatureReady(player2, new Millikin());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(indexOf(player2, blackBlocker), indexOf(player1, attacker)),
                new BlockerAssignment(indexOf(player2, artifactBlocker), indexOf(player1, attacker))));

        assertThat(blackBlocker.isBlocking()).isTrue();
        assertThat(artifactBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Loses the threshold bonus when the graveyard drops below seven cards")
    void losesThresholdBonusBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent crawler = addCreatureReady(player1, new Frightcrawler());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(1);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new WoodlandDruid(), new WoodlandDruid(), new WoodlandDruid(), new WoodlandDruid(),
                new WoodlandDruid(), new WoodlandDruid(), new WoodlandDruid());
    }
}
