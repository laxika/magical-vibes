package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one hasty token copy for each won flip and exiles them at the next end step")
    void createsHastyTokenCopyForEachWonFlipAndExilesThemAtEndStep() {
        harness.addToBattlefield(player1, new MirrorMarch());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        long wonFlips = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("wins the coin flip for Mirror March"))
                .count();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize((int) wonFlips);
        assertThat(tokens).allSatisfy(token -> assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .filteredOn(action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_STEP)
                .hasSize((int) wonFlips);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
