package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarteredCow.class, Forest.class, LightningBolt.class, Sift.class})
class BarteredCowTest extends BaseCardTest {

    @Test
    @DisplayName("When Bartered Cow dies, it creates a Food token")
    void deathCreatesFoodToken() {
        harness.addToBattlefield(player1, new BarteredCow());
        UUID cowId = harness.getPermanentId(player1, "Bartered Cow");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cowId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        assertThat(food.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        harness.assertInGraveyard(player1, "Bartered Cow");
    }

    @Test
    @DisplayName("When Bartered Cow is discarded, it creates a Food token")
    void discardCreatesFoodToken() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Sift(), new BarteredCow()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Bartered Cow");
    }

    @Test
    @DisplayName("Food created by Bartered Cow can be sacrificed for 3 life")
    void foodCanBeSacrificedForLife() {
        harness.addToBattlefield(player1, new BarteredCow());
        UUID cowId = harness.getPermanentId(player1, "Bartered Cow");
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, cowId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertNotOnBattlefield(player1, "Food");
    }
}
