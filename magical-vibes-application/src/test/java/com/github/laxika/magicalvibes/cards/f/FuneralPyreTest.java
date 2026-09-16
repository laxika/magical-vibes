package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BattleScreech;
import com.github.laxika.magicalvibes.cards.b.BenevolentBodyguard;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FuneralPyre.class, BattleScreech.class, BenevolentBodyguard.class})
class FuneralPyreTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any target card and its owner creates a 1/1 white flying Spirit")
    void exilesAnyCardAndCreatesTokenForOwner() {
        Card target = new BattleScreech();
        target.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(target));

        castFuneralPyre(target);

        harness.assertNotInGraveyard(player2, "Battle Screech");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Battle Screech"));
        assertSpiritToken(player2);
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("Creates the token for the card owner even when the card is in another graveyard")
    void createsTokenForCardOwner() {
        Card target = new BattleScreech();
        target.setOwnerId(player1.getId());
        harness.setGraveyard(player2, List.of(target));

        castFuneralPyre(target);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Battle Screech"));
        assertSpiritToken(player1);
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card target = new BattleScreech();
        target.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FuneralPyre()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player1, "Spirit");
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a graveyard card")
    void cannotTargetPermanent() {
        Card permanent = new BenevolentBodyguard();
        harness.addToBattlefield(player2, permanent);
        harness.setHand(player1, List.of(new FuneralPyre()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFuneralPyre(Card target) {
        harness.setHand(player1, List.of(new FuneralPyre()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
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
