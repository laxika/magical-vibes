package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecruitmentOfficerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability offers only creature cards with mana value 3 or less among the top four")
    void abilityOffersEligibleCreatures() {
        Card bears = new GrizzlyBears();
        setupTopCards(List.of(new HillGiant(), bears, new Plains(), new Shock()));
        activateAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).hasSize(4);
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing an eligible creature puts it into hand and bottoms the rest")
    void choosingCreaturePutsItIntoHand() {
        Card bears = new GrizzlyBears();
        setupTopCards(List.of(bears, new HillGiant(), new Plains(), new Shock()));
        activateAndResolve();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible creature among the top four, the ability bottoms all four")
    void noEligibleCreatureNeedsNoChoice() {
        setupTopCards(List.of(new HillGiant(), new Plains(), new Shock(), new Plains()));
        activateAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void activateAndResolve() {
        harness.addToBattlefield(player1, new RecruitmentOfficer());
        Permanent officer = gd.playerBattlefields.get(player1.getId()).getFirst();
        officer.setSummoningSick(false);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
