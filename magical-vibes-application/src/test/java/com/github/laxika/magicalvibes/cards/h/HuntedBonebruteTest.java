package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntedBonebrute.class, Shock.class})
class HuntedBonebruteTest extends BaseCardTest {

    @Test
    @DisplayName("Hunted Bonebrute's ETB only offers opponents as targets")
    void etbTargetsOpponent() {
        castBonebrute();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Hunted Bonebrute's ETB gives the targeted opponent two Dogs")
    void etbCreatesDogsForTargetedOpponent() {
        castBonebrute();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        List<Permanent> dogs = findPermanents(player2, "Dog");
        assertThat(dogs).hasSize(2);
        assertThat(dogs).allSatisfy(dog -> {
            assertThat(dog.getCard().isToken()).isTrue();
            assertThat(dog.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(dog.getCard().getSubtypes()).contains(CardSubtype.DOG);
            assertThat(dog.getEffectivePower()).isEqualTo(1);
            assertThat(dog.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("When Hunted Bonebrute dies, each opponent loses 3 life")
    void deathTriggerMakesEachOpponentLoseLife() {
        castBonebrute();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        UUID bonebruteId = harness.getPermanentId(player1, "Hunted Bonebrute");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, bonebruteId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void castBonebrute() {
        harness.setHand(player1, List.of(new HuntedBonebrute()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
