package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctuaryRaptor.class})
class SanctuaryRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and first strike when attacking with three tokens")
    void getsBoostAndFirstStrikeWithThreeTokens() {
        Permanent raptor = addRaptor();
        addToken("Token One");
        addToken("Token Two");
        addToken("Token Three");

        declareRaptorAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(raptor.getPowerModifier()).isEqualTo(2);
        assertThat(raptor.getToughnessModifier()).isZero();
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger with fewer than three tokens")
    void doesNotTriggerWithFewerThanThreeTokens() {
        Permanent raptor = addRaptor();
        addToken("Token One");
        addToken("Token Two");

        declareRaptorAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(raptor.getPowerModifier()).isZero();
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        Permanent raptor = addRaptor();
        addToken("Token One");
        addToken("Token Two");
        addToken("Token Three");

        declareRaptorAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raptor.getPowerModifier()).isZero();
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addRaptor() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new SanctuaryRaptor());
        raptor.setSummoningSick(false);
        return raptor;
    }

    private void addToken(String name) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setToken(true);
        token.setPower(1);
        token.setToughness(1);
        harness.addToBattlefield(player1, token);
    }

    private void declareRaptorAttackers(List<Integer> attackers) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, attackers);
    }
}
