package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrightcrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 and cannot block at threshold")
    void getsThresholdBonusAndCannotBlock() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent crawler = addCreatureReady(player1, new Frightcrawler());

        assertThat(gqs.getEffectivePower(gd, crawler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crawler)).isEqualTo(3);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        beginBlockerDeclaration();

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

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        beginBlockerDeclaration();

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

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private List<com.github.laxika.magicalvibes.model.Card> graveyardWithSevenCards() {
        return List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }
}
