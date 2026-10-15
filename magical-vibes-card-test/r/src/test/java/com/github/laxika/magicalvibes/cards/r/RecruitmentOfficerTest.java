package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirliftChaplain;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RecruitmentOfficer.class, GrizzlyBears.class, HillGiant.class, Plains.class, Shock.class,
        AirliftChaplain.class})
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
        harness.setLibrary(player1, cards);
    }

    @Test
    @DisplayName("The optional choice can be declined and only the top four move to the bottom")
    void decliningKeepsUnlookedCardsOnTop() {
        Card creature = new RecruitmentOfficer();
        Card land = new Plains();
        Card spell = new Shock();
        Card giant = new HillGiant();
        Card fifth = new Plains();
        Card sixth = new Plains();
        setupTopCards(List.of(creature, land, spell, giant, fifth, sixth));
        List<Card> handBefore = List.copyOf(gd.playerHands.get(player1.getId()));
        activateAndResolve();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(handBefore);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2)).containsExactly(fifth, sixth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 6))
                .containsExactlyInAnyOrder(creature, land, spell, giant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A creature with mana value exactly three can be chosen from a short library")
    void choosesManaValueThreeFromShortLibrary() {
        Card chaplain = new AirliftChaplain();
        Card land = new Plains();
        setupTopCards(List.of(chaplain, land));
        activateAndResolve();

        harness.handleMultipleCardsChosen(player1, List.of(chaplain.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chaplain);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice")
    void emptyLibraryResolves() {
        setupTopCards(List.of());
        activateAndResolve();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a creature leaves unlooked cards on top and bottoms only the other looked-at cards")
    void choosingPreservesUnlookedCards() {
        Card creature = new RecruitmentOfficer();
        Card first = new Plains();
        Card second = new Plains();
        Card third = new Plains();
        Card fifth = new RecruitmentOfficer();
        setupTopCards(List.of(creature, first, second, third, fifth));
        activateAndResolve();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature).doesNotContain(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability can be activated while summoning sick and does not tap its source")
    void activatesWhileSummoningSick() {
        setupTopCards(List.of());
        harness.addToBattlefield(player1, new RecruitmentOfficer());
        Permanent officer = gd.playerBattlefields.get(player1.getId()).getFirst();
        officer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(officer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
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
