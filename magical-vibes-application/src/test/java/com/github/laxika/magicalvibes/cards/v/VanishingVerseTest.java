package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VanishingVerseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a monocolored permanent")
    void exilesMonocoloredPermanent() {
        Permanent target = addPermanent(player2, "Monocolored Permanent", CardColor.GREEN);

        prepare();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Monocolored Permanent");
        harness.assertNotInGraveyard(player2, "Monocolored Permanent");
    }

    @Test
    @DisplayName("Cannot target a multicolored permanent")
    void cannotTargetMulticoloredPermanent() {
        Permanent target = addPermanent(player2, "Multicolored Permanent", CardColor.GREEN, CardColor.WHITE);

        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a colorless permanent")
    void cannotTargetColorlessPermanent() {
        Permanent target = addPermanent(player2, "Colorless Permanent");

        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepare() {
        harness.setHand(player1, List.of(new VanishingVerse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addPermanent(Player player, String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setColors(List.of(colors));
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
