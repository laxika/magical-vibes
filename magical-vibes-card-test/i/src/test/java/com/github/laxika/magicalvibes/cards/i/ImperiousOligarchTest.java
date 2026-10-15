package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({ImperiousOligarch.class, WrathOfGod.class})
class ImperiousOligarchTest extends BaseCardTest {

    @Test
    @DisplayName("Afterlife 1 creates a 1/1 white and black Spirit token with flying")
    void afterlifeCreatesSpiritToken() {
        harness.addToBattlefield(player1, new ImperiousOligarch());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Imperious Oligarch");
        List<Permanent> tokens = findPermanents(player1, "Spirit");

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Simultaneous deaths create one Spirit for each Oligarch's controller after the destruction resolves")
    void simultaneousDeathsCreateTokensForEachController() {
        harness.addToBattlefield(player1, new ImperiousOligarch());
        harness.addToBattlefield(player1, new ImperiousOligarch());
        harness.addToBattlefield(player2, new ImperiousOligarch());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Imperious Oligarch");
        harness.assertNotOnBattlefield(player2, "Imperious Oligarch");
        harness.assertInGraveyard(player1, "Imperious Oligarch");
        harness.assertInGraveyard(player2, "Imperious Oligarch");
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(countPermanents(player2, "Spirit")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
    }

    @Test
    @DisplayName("Vigilance keeps Imperious Oligarch untapped when attacking")
    void vigilancePreventsTapWhenAttacking() {
        Permanent oligarch = addCreatureReady(player1, new ImperiousOligarch());

        declareAttackers(player1, List.of(0));

        assertThat(oligarch.isTapped()).isFalse();
    }
}
