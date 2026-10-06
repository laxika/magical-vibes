package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({SendInThePest.class})
class SendInThePestTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent discards a card and you create a Pest token")
    void discardsAndCreatesPest() {
        Card discarded = new SendInThePest();
        harness.setHand(player1, List.of(new SendInThePest()));
        harness.setHand(player2, List.of(discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Pest trigger gains 1 life when it attacks")
    void pestGainsLifeWhenAttacking() {
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SendInThePest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent pest = findPermanent(player1, "Pest");
        pest.setSummoningSick(false);

        int pestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pest);
        declareAttackers(player1, List.of(pestIndex));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("An empty opposing hand does not prevent creating a Pest")
    void createsPestWithEmptyOpposingHand() {
        harness.setHand(player1, List.of(new SendInThePest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Pest")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        Permanent pest = findPermanent(player1, "Pest");
        assertThat(pest.isTapped()).isFalse();
        assertThat(pest.getCard().isToken()).isTrue();
        assertThat(pest.getCard().getPower()).isEqualTo(1);
        assertThat(pest.getCard().getToughness()).isEqualTo(1);
        assertThat(pest.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(pest.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(pest.getCard().getSubtypes()).containsExactly(CardSubtype.PEST);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The opponent chooses one card to discard and the caster keeps their remaining hand")
    void opponentChoosesOneCardToDiscard() {
        Card keptByCaster = new SendInThePest();
        Card keptByOpponent = new SendInThePest();
        Card discarded = new SendInThePest();
        harness.setHand(player1, List.of(new SendInThePest(), keptByCaster));
        harness.setHand(player2, List.of(keptByOpponent, discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptByCaster);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(keptByOpponent);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(countPermanents(player1, "Pest")).isEqualTo(1);
    }
}
