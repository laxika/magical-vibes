package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CallOfTheHerd.class)
class CallOfTheHerdTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Call of the Herd creates a 3/3 green Elephant token")
    void createsElephantToken() {
        harness.castFromHand(player1, new CallOfTheHerd(), "{2}{G}");
        harness.passBothPriorities();

        List<Permanent> elephants = elephantTokens();
        assertThat(elephants).hasSize(1);
        assertThat(elephants.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephants.getFirst().getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
        harness.assertInGraveyard(player1, "Call of the Herd");
    }

    @Test
    @DisplayName("Flashback creates an Elephant token and exiles Call of the Herd")
    void flashbackCreatesElephantAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new CallOfTheHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(elephantTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Call of the Herd");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Call of the Herd"));
    }

    @Test
    @DisplayName("The same card creates two Elephants when cast normally and then with flashback")
    void normalCastThenFlashbackCreatesTwoElephants() {
        CallOfTheHerd card = new CallOfTheHerd();
        harness.castFromHand(player1, card, "{2}{G}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Call of the Herd");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(elephantTokens()).hasSize(2);
        harness.assertNotInGraveyard(player1, "Call of the Herd");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.getPlayerBattlefield(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Flashback cannot be paid with the cheaper normal casting cost")
    void flashbackRequiresThreeGenericMana() {
        harness.setGraveyard(player1, List.of(new CallOfTheHerd()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Call of the Herd");
        assertThat(elephantTokens()).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback requires green mana")
    void flashbackRequiresGreenMana() {
        harness.setGraveyard(player1, List.of(new CallOfTheHerd()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Call of the Herd");
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> elephantTokens() {
        return findPermanents(player1, "Elephant").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
