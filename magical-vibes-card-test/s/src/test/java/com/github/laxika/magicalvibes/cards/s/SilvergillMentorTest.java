package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SilvergillMentor.class})
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
        harness.addToBattlefield(player1, new SilvergillMentor());
        Permanent merfolk = findPermanent(player1, "Silvergill Mentor");
        harness.setHand(player1, List.of(new SilvergillMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithBeholdPermanent(player1, 0, merfolk.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Merfolk"))
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Merfolk card in hand lets it be cast without the additional mana")
    void beholdMerfolkCardAvoidsAdditionalMana() {
        SilvergillMentor merfolk = new SilvergillMentor();
        harness.setHand(player1, List.of(new SilvergillMentor(), merfolk));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithBeholdHandCard(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(merfolk.getId()));
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(1);
        assertThat(gameLogContains("reveals Silvergill Mentor")).isTrue();
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

    @Test
    @DisplayName("The caster can pay the additional mana instead of revealing an available Merfolk")
    void canPayInsteadOfBeholdingCardInHand() {
        SilvergillMentor merfolk = new SilvergillMentor();
        harness.setHand(player1, List.of(new SilvergillMentor(), merfolk));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(merfolk);
        assertThat(gameLogContains("reveals")).isFalse();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Merfolk does not satisfy the additional cost")
    void opponentsMerfolkDoesNotAvoidAdditionalMana() {
        harness.addToBattlefield(player2, new SilvergillMentor());
        harness.setHand(player2, List.of(new SilvergillMentor()));
        harness.setHand(player1, List.of(new SilvergillMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Merfolk token is created when the triggered ability resolves")
    void tokenWaitsForEnterTheBattlefieldTrigger() {
        harness.setHand(player1, List.of(new SilvergillMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThat(countPermanents(player1, "Merfolk")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Silvergill Mentor")).isEqualTo(1);
        assertThat(countPermanents(player1, "Merfolk")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Merfolk")).isEqualTo(1);
        assertThat(countPermanents(player2, "Merfolk")).isZero();
        assertThat(findPermanent(player1, "Merfolk").isTapped()).isFalse();
    }

    private void castMentor() {
        harness.setHand(player1, List.of(new SilvergillMentor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
