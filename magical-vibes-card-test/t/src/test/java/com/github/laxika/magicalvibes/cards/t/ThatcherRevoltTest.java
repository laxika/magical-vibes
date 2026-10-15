package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Thatcher Revolt")
@CardUsed({ThatcherRevolt.class, ThrabenValiant.class})
class ThatcherRevoltTest extends BaseCardTest {

    private void castRevolt() {
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Creates three 1/1 Human tokens with haste")
    void createsThreeHastyHumans() {
        castRevolt();

        List<Permanent> humans = findPermanents(player1, "Human");

        assertThat(humans).hasSize(3);
        assertThat(humans).allSatisfy(human -> {
            assertThat(human.getCard().getPower()).isEqualTo(1);
            assertThat(human.getCard().getToughness()).isEqualTo(1);
            assertThat(human.getCard().getKeywords()).contains(Keyword.HASTE);
            assertThat(human.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(human.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(human.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
        });
    }

    @Test
    @DisplayName("The tokens are sacrificed at the beginning of the next end step")
    void tokensSacrificedAtNextEndStep() {
        castRevolt();
        harness.assertOnBattlefield(player1, "Human");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Human");
    }

    @Test
    @DisplayName("Newly created tokens can attack immediately")
    void tokensCanAttackImmediately() {
        castRevolt();

        declareAttackers(List.of(0, 1, 2));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Sacrificing the tokens leaves an unrelated Human on the battlefield")
    void doesNotSacrificeOtherHumans() {
        harness.addToBattlefield(player1, new ThrabenValiant());
        castRevolt();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Human");
        harness.assertOnBattlefield(player1, "Thraben Valiant");
    }

    @Test
    @DisplayName("The next end step sacrifices the tokens even on the opponent's turn")
    void sacrificesAtOpponentsEndStep() {
        castRevolt();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Human");
    }
}
