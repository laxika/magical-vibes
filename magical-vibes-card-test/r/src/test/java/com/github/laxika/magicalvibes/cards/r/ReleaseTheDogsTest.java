package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed(ReleaseTheDogs.class)
class ReleaseTheDogsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates four 1/1 white Dog creature tokens")
    void createsFourDogs() {
        harness.setHand(player1, List.of(new ReleaseTheDogs()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> dogs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(dogs).hasSize(4);
        assertThat(dogs).allSatisfy(dog -> {
            assertThat(dog.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(dog.getCard().getPower()).isEqualTo(1);
            assertThat(dog.getCard().getToughness()).isEqualTo(1);
            assertThat(dog.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(dog.getCard().getSubtypes()).contains(CardSubtype.DOG);
        });
    }

    @Test
    @DisplayName("Creates the Dogs for the caster when player two casts the spell")
    void createsDogsForPlayerTwo() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ReleaseTheDogs()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(4).allSatisfy(dog -> {
            assertThat(dog.getCard().isToken()).isTrue();
            assertThat(dog.getCard().getSubtypes()).contains(CardSubtype.DOG);
            assertThat(dog.isTapped()).isFalse();
            assertThat(dog.isSummoningSick()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof ReleaseTheDogs);
    }
}
