package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({EvenTheOdds.class, GrizzlyBears.class})
class EvenTheOddsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates three 1/1 white Soldier tokens when the opponent controls more creatures")
    void createsThreeSoldierTokensWhenBehindOnCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EvenTheOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> soldiers = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(soldiers).hasSize(3).allSatisfy(soldier -> {
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        });
    }

    @Test
    @DisplayName("Cannot cast when creature counts are tied")
    void cannotCastWhenCreatureCountsAreTied() {
        harness.setHand(player1, List.of(new EvenTheOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot cast when you control more creatures")
    void cannotCastWhenAheadOnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EvenTheOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
