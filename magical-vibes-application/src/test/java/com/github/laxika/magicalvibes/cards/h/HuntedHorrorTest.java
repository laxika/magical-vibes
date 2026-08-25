package com.github.laxika.magicalvibes.cards.h;

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

@CardUsed(HuntedHorror.class)
class HuntedHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 3/3 green Centaur tokens with protection from black under the targeted opponent's control")
    void etbCreatesCentaurTokensForTargetOpponent() {
        harness.setHand(player1, List.of(new HuntedHorror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> centaurs = findPermanents(player2, "Centaur");
        assertThat(centaurs).hasSize(2);
        assertThat(findPermanents(player1, "Centaur")).isEmpty();

        for (Permanent centaur : centaurs) {
            assertThat(centaur.getCard().isToken()).isTrue();
            assertThat(centaur.getCard().getPower()).isEqualTo(3);
            assertThat(centaur.getCard().getToughness()).isEqualTo(3);
            assertThat(centaur.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(centaur.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(centaur.getCard().getSubtypes()).containsExactly(CardSubtype.CENTAUR);
            assertThat(gqs.hasProtectionFrom(gd, centaur, CardColor.BLACK)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, centaur, CardColor.GREEN)).isFalse();
        }
    }

    @Test
    @DisplayName("Cannot target the controller with the ETB ability")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new HuntedHorror()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
