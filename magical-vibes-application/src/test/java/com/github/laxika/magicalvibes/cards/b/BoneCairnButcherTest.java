package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Bone-Cairn Butcher")
@CardUsed(BoneCairnButcher.class)
class BoneCairnButcherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two tapped and attacking Warrior tokens with deathtouch")
    void attackingCreatesTwoDeathtouchWarriorTokens() {
        addButcherReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
            token.setAttacking(true);
            assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue();
        });
    }

    @Test
    @DisplayName("Mobilized tokens are sacrificed at the beginning of the next end step")
    void mobilizedTokensAreSacrificedAtNextEndStep() {
        addButcherReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    private Permanent addButcherReady(Player player) {
        Permanent permanent = new Permanent(new BoneCairnButcher());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
