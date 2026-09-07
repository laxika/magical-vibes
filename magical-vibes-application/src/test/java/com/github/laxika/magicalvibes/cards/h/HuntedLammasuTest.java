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

@CardUsed(HuntedLammasu.class)
class HuntedLammasuTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 4/4 black Horror token under the targeted opponent's control")
    void etbCreatesHorrorTokenForTargetOpponent() {
        harness.setHand(player1, List.of(new HuntedLammasu()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> horrors = findPermanents(player2, "Horror");
        assertThat(horrors).hasSize(1);
        assertThat(findPermanents(player1, "Horror")).isEmpty();

        Permanent horror = horrors.getFirst();
        assertThat(horror.getCard().isToken()).isTrue();
        assertThat(horror.getCard().getPower()).isEqualTo(4);
        assertThat(horror.getCard().getToughness()).isEqualTo(4);
        assertThat(horror.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(horror.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(horror.getCard().getSubtypes()).containsExactly(CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Cannot target the controller with the ETB ability")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new HuntedLammasu()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
