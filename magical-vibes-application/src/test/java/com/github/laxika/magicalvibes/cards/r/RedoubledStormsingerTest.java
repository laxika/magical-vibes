package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedoubledStormsinger.class, GrizzlyBears.class})
class RedoubledStormsingerTest extends BaseCardTest {

    @Test
    @DisplayName("Copies creature tokens that entered this turn tapped and attacking")
    void copiesEnteredTokensTappedAndAttacking() {
        Permanent stormsinger = addCreatureReady(player1, new RedoubledStormsinger());
        Permanent originalToken = addEnteredCreatureToken(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());

        List<Permanent> tokens = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        Permanent copy = tokens.stream()
                .filter(permanent -> permanent != originalToken)
                .findFirst()
                .orElseThrow();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.isAttacking()).isTrue();
        assertThat(copy.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(stormsinger.isAttacking()).isTrue();

        gs.declareBlockers(gd, player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(originalToken);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(copy);
    }

    @Test
    @DisplayName("Does not copy a creature token that did not enter this turn")
    void ignoresOlderToken() {
        addCreatureReady(player1, new RedoubledStormsinger());
        Card olderToken = new GrizzlyBears();
        olderToken.setToken(true);
        addCreatureReady(player1, olderToken);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    private Permanent addEnteredCreatureToken(Player player) {
        Card token = new GrizzlyBears();
        token.setToken(true);
        Permanent permanent = harness.enterBattlefieldAndReturn(player, token);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
