package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PerilousVoyage;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SailorOfMeans.class, PerilousVoyage.class})
class SailorOfMeansTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sailor of Means puts it on the battlefield and triggers ETB")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new SailorOfMeans(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sailor of Means");
    }

    @Test
    @DisplayName("When Sailor of Means enters, one Treasure token is created")
    void etbCreatesOneTreasureToken() {
        harness.castFromHand(player1, new SailorOfMeans(), "{2}{U}");
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Treasure");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("ETB Treasure token is an artifact with Treasure subtype")
    void tokenHasCorrectProperties() {
        harness.castFromHand(player1, new SailorOfMeans(), "{2}{U}");
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Treasure");

        assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Battlefield has Sailor of Means plus one Treasure token after ETB resolves")
    void battlefieldHasAllPermanents() {
        harness.castFromHand(player1, new SailorOfMeans(), "{2}{U}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A new Treasure can immediately be sacrificed for one mana of any color")
    void treasureTokenProducesMana(ManaColor color) {
        harness.castFromHand(player1, new SailorOfMeans(), "{2}{U}");
        resolveAllTriggers();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.isTapped()).isFalse();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, index, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sailor of Means");
    }

    @Test
    @DisplayName("The enter trigger creates a Treasure for its controller after Sailor leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.castFromHand(player1, new SailorOfMeans(), "{2}{U}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Sailor of Means"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Sailor of Means");
        harness.assertInHand(player1, "Sailor of Means");

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}