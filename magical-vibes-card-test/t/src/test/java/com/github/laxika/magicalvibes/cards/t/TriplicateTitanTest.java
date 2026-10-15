package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriplicateTitan.class, WrathOfGod.class})
class TriplicateTitanTest extends BaseCardTest {

    @Test
    void deathTriggerCreatesThreeDistinctGolemTokens() {
        harness.addToBattlefield(player1, new TriplicateTitan());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Golem")).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        List<Permanent> tokens = findPermanents(player1, "Golem");
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.GOLEM);
            assertThat(token.getCard().isToken()).isTrue();
        });
        assertThat(tokens).anyMatch(token -> token.hasKeyword(Keyword.FLYING));
        assertThat(tokens).anyMatch(token -> token.hasKeyword(Keyword.VIGILANCE));
        assertThat(tokens).anyMatch(token -> token.hasKeyword(Keyword.TRAMPLE));
    }

    @Test
    void lethalDamageCreatesTokensForTheDyingTitansController() {
        Permanent titan = harness.addToBattlefieldAndReturn(player2, new TriplicateTitan());
        titan.setMarkedDamage(9);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Triplicate Titan")).isEmpty();
        assertThat(findPermanents(player2, "Golem")).hasSize(3);
        assertThat(findPermanents(player1, "Golem")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void simultaneousTitanDeathsProduceOneCompleteTokenGroupPerResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TriplicateTitan());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TriplicateTitan());
        first.setMarkedDamage(9);
        second.setMarkedDamage(9);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Golem")).hasSize(3);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Golem")).hasSize(6);
        assertThat(gd.stack).isEmpty();
    }
}
