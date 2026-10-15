package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EagerFirstYear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreativeOutburst.class, EagerFirstYear.class})
class CreativeOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage and keeps one of the top five cards")
    void dealsDamageAndKeepsOneTopCard() {
        Card first = new EagerFirstYear();
        Card second = new EagerFirstYear();
        Card third = new EagerFirstYear();
        Card fourth = new EagerFirstYear();
        Card fifth = new EagerFirstYear();
        Card untouched = new EagerFirstYear();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, untouched));

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CreativeOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(third.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).contains(third);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(first, second, fourth, fifth);
        harness.assertInGraveyard(player1, "Creative Outburst");
    }

    @Test
    @DisplayName("The hand ability pays two hybrid mana, discards the source, and creates a Treasure")
    void handAbilityCreatesTreasure() {
        harness.setHand(player1, List.of(new CreativeOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Creative Outburst");
        harness.assertNotOnBattlefield(player1, "Treasure");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Treasure")).isNotNull();
        harness.assertInGraveyard(player1, "Creative Outburst");
    }

    @Test
    void shortLibraryStillRequiresOneCardAndBottomsTheOther() {
        Card first = new EagerFirstYear();
        Card second = new EagerFirstYear();
        harness.setLibrary(player1, List.of(first, second));
        castAtOpponent();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        harness.assertLife(player2, 15);
    }

    @Test
    void singleCardLibraryPutsItsCardIntoHand() {
        Card only = new EagerFirstYear();
        harness.setLibrary(player1, List.of(only));
        castAtOpponent();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Creative Outburst");
    }

    @Test
    void emptyLibraryDoesNotPreventDamage() {
        harness.setLibrary(player1, List.of());
        castAtOpponent();

        harness.assertLife(player2, 15);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Creative Outburst");
    }

    @Test
    void canDealLethalDamageToACreatureAndStillTakeALibraryCard() {
        Permanent target = addCreatureReady(player2, new EagerFirstYear());
        Card only = new EagerFirstYear();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new CreativeOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Eager First-Year");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
    }

    @Test
    void illegalDamageTargetPreventsTheLibraryEffect() {
        Permanent target = addCreatureReady(player2, new EagerFirstYear());
        Card only = new EagerFirstYear();
        harness.setLibrary(player1, List.of(only));
        harness.setHand(player1, List.of(new CreativeOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setHand(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Creative Outburst");
    }

    @Test
    void handAbilityAcceptsTwoRedMana() {
        harness.setHand(player1, List.of(new CreativeOutburst()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Treasure");
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Creative Outburst");
    }

    @Test
    void handAbilityAcceptsOneManaOfEachHybridColor() {
        harness.setHand(player1, List.of(new CreativeOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Treasure");
        harness.assertInGraveyard(player1, "Creative Outburst");
    }

    @Test
    void insufficientHybridManaDoesNotDiscardTheCard() {
        Card source = new CreativeOutburst();
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    private void castAtOpponent() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new CreativeOutburst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
