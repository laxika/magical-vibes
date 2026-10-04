package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SarcomiteMyr;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvenTheOdds.class, SarcomiteMyr.class})
class EvenTheOddsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 white Soldier tokens when its controller has fewer creatures")
    void createsSoldierTokensWhenControllerHasFewerCreatures() {
        addCreatureReady(player2, new SarcomiteMyr());

        harness.castFromHand(player1, new EvenTheOdds(), "{2}{W}");
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(3);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(soldier.getEffectivePower()).isEqualTo(1);
            assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Cannot be cast when both players control the same number of creatures")
    void cannotBeCastWithEqualCreatureCounts() {
        addCreatureReady(player1, new SarcomiteMyr());
        addCreatureReady(player2, new SarcomiteMyr());

        assertThatThrownBy(() -> harness.castFromHand(player1, new EvenTheOdds(), "{2}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot be cast when its controller controls more creatures")
    void cannotBeCastWithMoreCreatures() {
        addCreatureReady(player1, new SarcomiteMyr());

        assertThatThrownBy(() -> harness.castFromHand(player1, new EvenTheOdds(), "{2}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot be cast when neither player controls a creature")
    void cannotBeCastWithNoCreatures() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new EvenTheOdds(), "{2}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Still creates Soldiers if the opponent sacrifices their last creature in response")
    void resolvesAfterCreatureCountsBecomeEqual() {
        addCreatureReady(player2, new SarcomiteMyr());
        harness.setLibrary(player2, List.of(new SarcomiteMyr()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castFromHand(player1, new EvenTheOdds(), "{2}{W}");
        harness.activateAbility(player2, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(3);
        harness.assertInGraveyard(player1, "Even the Odds");
    }
}
