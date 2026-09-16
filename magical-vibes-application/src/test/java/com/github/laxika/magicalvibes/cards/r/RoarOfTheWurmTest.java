package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(RoarOfTheWurm.class)
class RoarOfTheWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Roar of the Wurm creates a 6/6 green Wurm token")
    void createsWurmToken() {
        harness.castFromHand(player1, new RoarOfTheWurm(), "{6}{G}");
        harness.passBothPriorities();

        List<Permanent> wurms = wurmTokens();
        assertThat(wurms).hasSize(1);
        Permanent wurm = wurms.getFirst();
        assertThat(wurm.getCard().getPower()).isEqualTo(6);
        assertThat(wurm.getCard().getToughness()).isEqualTo(6);
        assertThat(wurm.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wurm.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(wurm.getCard().getSubtypes()).contains(CardSubtype.WURM);
        harness.assertInGraveyard(player1, "Roar of the Wurm");
    }

    @Test
    @DisplayName("Flashback creates a Wurm token and exiles Roar of the Wurm")
    void flashbackCreatesWurmAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new RoarOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(wurmTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Roar of the Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Roar of the Wurm"));
    }

    @Test
    @DisplayName("Flashback requires three generic mana in addition to green mana")
    void flashbackRequiresThreeGenericMana() {
        harness.setGraveyard(player1, List.of(new RoarOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback requires green mana in addition to three generic mana")
    void flashbackRequiresGreenMana() {
        harness.setGraveyard(player1, List.of(new RoarOfTheWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> wurmTokens() {
        return findPermanents(player1, "Wurm").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
