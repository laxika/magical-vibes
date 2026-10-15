package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiredSphinx.class, Forest.class})
class InspiredSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Inspired Sphinx draws one card for its opponent")
    void entersAndDrawsForEachOpponent() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, new InspiredSphinx(), "{5}{U}{U}");
        harness.passBothPriorities();
        int handBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeTrigger + 1);
    }

    @Test
    @DisplayName("Inspired Sphinx creates a flying Thopter artifact creature token")
    void createsThopterToken() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new InspiredSphinx());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sphinx), 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Thopter");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opposing Inspired Sphinx draws for its controller when it enters without being cast")
    void enteringWithoutCastingDrawsForOpposingController() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player2, new InspiredSphinx());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped Inspired Sphinx can activate repeatedly without tapping or untapping")
    void tappedSphinxCanCreateMultipleTokens() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new InspiredSphinx());
        sphinx.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int sphinxIndex = gd.playerBattlefields.get(player1.getId()).indexOf(sphinx);

        harness.activateAbility(player1, sphinxIndex, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, sphinxIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.isTapped()).isFalse();
                });
        assertThat(sphinx.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
