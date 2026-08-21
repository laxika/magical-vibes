package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Dalkovan Packbeasts")
@CardUsed(DalkovanPackbeasts.class)
class DalkovanPackbeastsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates three tapped and attacking red Warrior tokens")
    void attackingCreatesWarriorTokens() {
        addCreatureReady(player1, new DalkovanPackbeasts());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Attack tokens are sacrificed at the beginning of the next end step")
    void attackTokensAreSacrificedAtNextEndStep() {
        addCreatureReady(player1, new DalkovanPackbeasts());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(3);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }
}
