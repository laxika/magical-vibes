package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ZurgoThundersDecree.class)
class ZurgoThundersDecreeTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two tapped and attacking Warrior tokens")
    void attackingCreatesTwoMobilizedTokens() {
        addZurgoReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = warriorTokens(player1);
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Warrior tokens survive their controller's next end step")
    void warriorTokensSurviveYourEndStep() {
        addZurgoReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToEndStep(player1);

        assertThat(warriorTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Warrior tokens are sacrificed at an opponent's end step")
    void warriorTokensAreSacrificedAtOpponentsEndStep() {
        addZurgoReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        advanceToEndStep(player2);

        assertThat(warriorTokens(player1)).isEmpty();
    }

    private Permanent addZurgoReady(Player player) {
        Permanent permanent = new Permanent(new ZurgoThundersDecree());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private List<Permanent> warriorTokens(Player player) {
        return findPermanents(player, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
