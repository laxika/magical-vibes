package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.carddata.CardPrintingRegistry;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WilyGoblin.class})
class WilyGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Wily Goblin puts it on the battlefield and triggers ETB")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wily Goblin");
    }

    @Test
    @DisplayName("When Wily Goblin enters, one Treasure token is created")
    void etbCreatesOneTreasureToken() {
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        var tokens = findPermanents(player1, "Treasure");
        assertThat(tokens).hasSize(1);
    }

    @Test
    @DisplayName("ETB Treasure token is an artifact with Treasure subtype")
    void tokenHasCorrectProperties() {
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent treasure = findPermanent(player1, "Treasure");

        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(treasure.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Treasure token art prefers the XLN token set when that printing is registered")
    void treasureTokenUsesSourceSetWhenRegistered() {
        CardPrintingRegistry.registerTokenImages("XLN", Map.of(
                CardPrintingRegistry.buildTokenKey("Treasure", null, null, null),
                new CardPrintingRegistry.TokenImageData("txln", "7")));

        WilyGoblin goblin = new WilyGoblin();
        goblin.setSetCode("XLN");
        goblin.setCollectorNumber("174");
        harness.castFromHand(player1, goblin, "{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().getSetCode()).isEqualTo("txln");
        assertThat(treasure.getCard().getCollectorNumber()).isEqualTo("7");
    }

    @Test
    @DisplayName("Battlefield has Wily Goblin plus one Treasure token after ETB resolves")
    void battlefieldHasAllPermanents() {
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("A newly created Treasure can be sacrificed immediately for one mana of any color")
    void treasureTokenHasManaAbility(ManaColor color) {
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent treasure = findPermanent(player1, "Treasure");

        assertThat(treasure.isTapped()).isFalse();
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);

        harness.assertNotOnBattlefield(player1, "Treasure");
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wily Goblin");
    }

    @Test
    @DisplayName("Treasure creation waits for the enter trigger to resolve and benefits only its controller")
    void treasureCreationWaitsForTriggerResolution() {
        harness.castFromHand(player1, new WilyGoblin(), "{R}{R}");

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wily Goblin");
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
