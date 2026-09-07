package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuneralPyre.class, GrizzlyBears.class, Shock.class})
class FuneralPyreTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any target card and its owner creates a 1/1 white flying Spirit")
    void exilesAnyCardAndCreatesTokenForOwner() {
        Card shock = new Shock();
        harness.setGraveyard(player2, new ArrayList<>(List.of(shock)));

        castFuneralPyre(shock);

        harness.assertNotInGraveyard(player2, "Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
        assertSpiritToken(player2);
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.setHand(player1, List.of(new FuneralPyre()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player1, "Spirit");
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a graveyard card")
    void cannotTargetPermanent() {
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);
        harness.setHand(player1, List.of(new FuneralPyre()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFuneralPyre(Card target) {
        harness.setHand(player1, List.of(new FuneralPyre()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void assertSpiritToken(Player owner) {
        assertThat(gd.playerBattlefields.get(owner.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Spirit")
                        && permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.WHITE
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT)
                        && permanent.getCard().getKeywords().contains(Keyword.FLYING));
    }
}
