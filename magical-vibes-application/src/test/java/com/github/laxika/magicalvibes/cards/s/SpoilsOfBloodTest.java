package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SpoilsOfBlood.class)
class SpoilsOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Creates no token when no creatures died this turn")
    void createsNoTokenWithoutCreatureDeaths() {
        castSpoilsOfBlood();

        assertThat(findPermanents(player1, "Horror")).isEmpty();
    }

    @Test
    @DisplayName("Creates one Horror token sized to all creature deaths this turn")
    void createsHorrorSizedToCreatureDeaths() {
        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);

        castSpoilsOfBlood();

        List<Permanent> horrors = findPermanents(player1, "Horror");
        assertThat(horrors).hasSize(1);
        Permanent horror = horrors.getFirst();
        assertThat(horror.getCard().isToken()).isTrue();
        assertThat(horror.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(horror.getCard().getSubtypes()).containsExactly(CardSubtype.HORROR);
        assertThat(horror.getEffectivePower()).isEqualTo(5);
        assertThat(horror.getEffectiveToughness()).isEqualTo(5);
    }

    private void castSpoilsOfBlood() {
        harness.setHand(player1, List.of(new SpoilsOfBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
