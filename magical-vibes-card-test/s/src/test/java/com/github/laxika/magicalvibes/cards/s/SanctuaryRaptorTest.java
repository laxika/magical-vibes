package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.u.UnholyHeat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctuaryRaptor.class, UnholyHeat.class})
class SanctuaryRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 and first strike when attacking with three tokens")
    void getsBoostAndFirstStrikeWithThreeTokens() {
        Permanent raptor = addRaptor();
        addToken("Token One");
        addToken("Token Two");
        addToken("Token Three");

        declareAttackers(List.of(0));
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

        declareAttackers(List.of(0));
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

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(raptor.getPowerModifier()).isZero();
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Token condition must still hold when the attack trigger resolves")
    void losesBonusIfThirdTokenDiesInResponse() {
        Permanent raptor = addRaptor();
        addToken("Token One");
        addToken("Token Two");
        addToken("Token Three");
        Permanent thirdToken = gd.playerBattlefields.get(player1.getId()).get(3);
        harness.setHand(player2, List.of(new UnholyHeat()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.castInstant(player2, 0, thirdToken.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(thirdToken);
        assertThat(raptor.getPowerModifier()).isZero();
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Creating a third token after attacking cannot create the trigger")
    void thirdTokenAfterAttackDoesNotCreateTrigger() {
        Permanent raptor = addRaptor();
        addToken("Token One");
        addToken("Token Two");

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).isEmpty();
            addToken("Token Three");
            resolveAllTriggers();
        });

        assertThat(raptor.getPowerModifier()).isZero();
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Opponent tokens do not count toward the condition")
    void opponentTokensDoNotCount() {
        Permanent raptor = addRaptor();
        addToken("Token One");
        addToken("Token Two");
        addToken("Opponent Token");
        Permanent opponentToken = gd.playerBattlefields.get(player1.getId()).remove(3);
        gd.playerBattlefields.get(player2.getId()).add(opponentToken);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(raptor.getPowerModifier()).isZero();
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Noncreature tokens also satisfy the condition")
    void noncreatureTokensCount() {
        Permanent raptor = addRaptor();
        for (int i = 0; i < 3; i++) {
            Card treasure = new Card();
            treasure.setName("Treasure");
            treasure.setType(CardType.ARTIFACT);
            treasure.setToken(true);
            harness.addToBattlefield(player1, treasure);
        }

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(raptor.getPowerModifier()).isEqualTo(2);
        assertThat(raptor.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
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

}
