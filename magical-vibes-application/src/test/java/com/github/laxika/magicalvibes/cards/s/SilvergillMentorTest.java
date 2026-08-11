package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SilvergillMentorTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Merfolk, casting requires the additional {2}")
    void requiresAdditionalManaWithoutMerfolk() {
        harness.setHand(player1, List.of(new SilvergillMentor()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Merfolk permanent lets it be cast without the additional mana")
    void beholdMerfolkPermanentAvoidsAdditionalMana() {
        harness.addToBattlefield(player1, new MerfolkOfThePearlTrident());
        castMentor();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Merfolk"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Merfolk card in hand lets it be cast without the additional mana")
    void beholdMerfolkCardAvoidsAdditionalMana() {
        MerfolkOfThePearlTrident merfolk = new MerfolkOfThePearlTrident();
        harness.setHand(player1, List.of(new SilvergillMentor(), merfolk));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(merfolk.getId()));
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter-the-battlefield ability creates a 1/1 white and blue Merfolk")
    void createsWhiteAndBlueMerfolkToken() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        castMentor();

        Permanent token = findPermanent(player1, "Merfolk");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.MERFOLK);
        assertThat(token.getCard().isToken()).isTrue();
    }

    private void castMentor() {
        harness.setHand(player1, List.of(new SilvergillMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
