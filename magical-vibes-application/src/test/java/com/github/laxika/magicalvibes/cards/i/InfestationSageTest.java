package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfestationSage.class, WrathOfGod.class})
class InfestationSageTest extends BaseCardTest {

    @Test
    @DisplayName("When Infestation Sage dies, it creates a 1/1 black and green Insect with flying")
    void deathCreatesFlyingInsectToken() {
        harness.addToBattlefield(player1, new InfestationSage());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Infestation Sage");
        Permanent token = findPermanent(player1, "Insect");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Simultaneous deaths create one Insect per Sage for each controller")
    void simultaneousDeathsCreateTokensForEachController() {
        harness.addToBattlefield(player1, new InfestationSage());
        harness.addToBattlefield(player1, new InfestationSage());
        harness.addToBattlefield(player2, new InfestationSage());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Infestation Sage")).isZero();
        assertThat(countPermanents(player2, "Infestation Sage")).isZero();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
        assertThat(countPermanents(player2, "Insect")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Infestation Sage");
        harness.assertInGraveyard(player2, "Infestation Sage");
    }

    @Test
    @DisplayName("The Insect does not inherit the Sage's death trigger")
    void insectDeathDoesNotCreateAnotherToken() {
        harness.addToBattlefield(player1, new InfestationSage());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
