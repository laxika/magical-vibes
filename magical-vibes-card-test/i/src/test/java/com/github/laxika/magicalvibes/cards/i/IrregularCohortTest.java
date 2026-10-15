package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.ForceOfDespair;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrregularCohort.class, ForceOfDespair.class})
class IrregularCohortTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a colorless 2/2 Shapeshifter token with changeling")
    void etbCreatesChangelingShapeshifterToken() {
        harness.castFromHand(player1, new IrregularCohort(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Shapeshifter")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).containsExactly(Keyword.CHANGELING);
    }

    @Test
    @DisplayName("The token is created only when the enter ability resolves")
    void tokenCreationUsesTheStack() {
        harness.castFromHand(player1, new IrregularCohort(), "{2}{W}{W}");
        assertThat(countPermanents(player1, "Shapeshifter")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Irregular Cohort")).isEqualTo(1);
        assertThat(countPermanents(player1, "Shapeshifter")).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Shapeshifter")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The opponent receives the token when they cast the Cohort")
    void tokenBelongsToTheAbilityController() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new IrregularCohort(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Shapeshifter")).isEqualTo(1);
        assertThat(countPermanents(player1, "Shapeshifter")).isZero();
    }

    @Test
    @DisplayName("Cohort and its token have creature types beyond Shapeshifter")
    void changelingAppliesToBothCreatures() {
        harness.castFromHand(player1, new IrregularCohort(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.hasEffectiveSubtype(gd, permanent, CardSubtype.GOBLIN)).isTrue();
            assertThat(gqs.hasEffectiveSubtype(gd, permanent, CardSubtype.ELF)).isTrue();
        }
    }

    @Test
    @DisplayName("The token ability resolves after the Cohort is destroyed")
    void tokenCreationSurvivesSourceRemoval() {
        harness.castFromHand(player1, new IrregularCohort(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.castFromHand(player2, new ForceOfDespair(), "{1}{B}{B}");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Irregular Cohort")).isZero();
        assertThat(countPermanents(player1, "Shapeshifter")).isZero();

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Shapeshifter")).isEqualTo(1);
    }
}
