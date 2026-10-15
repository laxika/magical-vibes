package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BumpInTheNight.class})
class BumpInTheNightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bump in the Night makes target opponent lose 3 life")
    void targetOpponentLoses3Life() {
        harness.setHand(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target yourself with Bump in the Night")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Bump in the Night goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Bump in the Night");
    }

    @Test
    @DisplayName("Flashback from graveyard makes target opponent lose 3 life")
    void flashbackMakesOpponentLoseLife() {
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Bump in the Night");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bump in the Night"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as sorcery spell")
    void flashbackPutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bump in the Night");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback cannot target its caster")
    void flashbackCannotTargetYourself() {
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
        harness.assertInGraveyard(player1, "Bump in the Night");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Flashback requires red mana even when six mana are available")
    void flashbackRequiresRedMana() {
        harness.setGraveyard(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bump in the Night");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("The same card can be cast normally and then flashed back")
    void normalCastThenFlashback() {
        BumpInTheNight card = new BumpInTheNight();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertInGraveyard(player1, "Bump in the Night");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 20);
        harness.assertNotInGraveyard(player1, "Bump in the Night");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getId()).contains(card.getId());
    }
}
