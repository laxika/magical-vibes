package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed({ProsperousPirates.class, PerilousVoyage.class})
class ProsperousPiratesTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Prosperous Pirates puts it on the battlefield and triggers ETB")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new ProsperousPirates(), "{4}{U}");
        harness.passBothPriorities(); // Resolve creature â€” ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Prosperous Pirates");
    }

    @Test
    @DisplayName("When Prosperous Pirates enters, two Treasure tokens are created")
    void etbCreatesTwoTreasureTokens() {
        harness.castFromHand(player1, new ProsperousPirates(), "{4}{U}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        List<Permanent> tokens = findPermanents(player1, "Treasure");
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("ETB Treasure tokens are artifacts with Treasure subtype")
    void tokensHaveCorrectProperties() {
        harness.castFromHand(player1, new ProsperousPirates(), "{4}{U}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent token = findPermanent(player1, "Treasure");

        assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Battlefield has Prosperous Pirates plus two Treasure tokens after ETB resolves")
    void battlefieldHasAllPermanents() {
        harness.castFromHand(player1, new ProsperousPirates(), "{4}{U}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Each new Treasure can immediately be sacrificed for one mana of any color")
    void treasureTokensProduceMana(ManaColor color) {
        harness.castFromHand(player1, new ProsperousPirates(), "{4}{U}");
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(2).allSatisfy(treasure -> assertThat(treasure.isTapped()).isFalse());
        for (int i = 0; i < 2; i++) {
            Permanent treasure = findPermanent(player1, "Treasure");
            int index = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
            harness.activateAbility(player1, index, null, null);
            harness.handleListChoice(player1, color.name());

            assertThat(findPermanents(player1, "Treasure")).hasSize(1 - i);
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(i + 1);
            assertThat(gd.stack).isEmpty();
        }
        harness.assertOnBattlefield(player1, "Prosperous Pirates");
    }

    @Test
    @DisplayName("The enter trigger creates Treasures for its controller even after Pirates leaves")
    void triggerResolvesAfterSourceLeaves() {
        harness.castFromHand(player1, new ProsperousPirates(), "{4}{U}");
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.setHand(player2, List.of(new PerilousVoyage()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Prosperous Pirates"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Prosperous Pirates");
        harness.assertInHand(player1, "Prosperous Pirates");

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }
}
