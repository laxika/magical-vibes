package com.github.laxika.magicalvibes.cards.b;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(BeastAttack.class)
class BeastAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Beast Attack creates a 4/4 green Beast token")
    void createsBeastToken() {
        harness.castFromHand(player1, new BeastAttack(), "{2}{G}{G}{G}");

        harness.passBothPriorities();

        List<Permanent> beasts = beastTokens();
        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getCard().getPower()).isEqualTo(4);
        assertThat(beasts.getFirst().getCard().getToughness()).isEqualTo(4);
        assertThat(beasts.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beasts.getFirst().getCard().getSubtypes()).contains(CardSubtype.BEAST);
        harness.assertInGraveyard(player1, "Beast Attack");
    }

    @Test
    @DisplayName("Flashback creates a Beast token and exiles Beast Attack")
    void flashbackCreatesBeastAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new BeastAttack()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(beastTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Beast Attack");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Beast Attack"));
    }

    @Test
    @DisplayName("The same Beast Attack creates a second Beast when flashed back after resolving from hand")
    void canFlashBackAfterCastingFromHand() {
        harness.castFromHand(player1, new BeastAttack(), "{2}{G}{G}{G}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Beast Attack");
        assertThat(beastTokens()).hasSize(1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(beastTokens()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Beast Attack");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Beast Attack"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Flashback requires three green mana even when five total mana are available")
    void flashbackRejectsInsufficientGreenMana() {
        harness.setGraveyard(player1, List.of(new BeastAttack()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Beast Attack");
        assertThat(beastTokens()).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> beastTokens() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Beast"))
                .toList();
    }
}
