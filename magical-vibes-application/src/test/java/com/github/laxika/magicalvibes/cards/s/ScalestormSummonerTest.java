package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScalestormSummoner.class, AirElemental.class})
class ScalestormSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a 3/1 red Dinosaur with a power-4 creature")
    void createsDinosaurWithPower4Creature() {
        addCreatureReady(player1, new ScalestormSummoner());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Dinosaur").getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Attacking does not create a Dinosaur without a power-4 creature")
    void doesNotCreateDinosaurWithoutPower4Creature() {
        addCreatureReady(player1, new ScalestormSummoner());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
    }

    @Test
    @DisplayName("The power condition is checked when the attack trigger resolves")
    void rechecksPowerConditionAtResolution() {
        addCreatureReady(player1, new ScalestormSummoner());
        Permanent airElemental = addCreatureReady(player1, new AirElemental());

        declareAttackers(player1, List.of(0));
        airElemental.setPowerModifier(-1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Dinosaur")).isEmpty();
    }
}
