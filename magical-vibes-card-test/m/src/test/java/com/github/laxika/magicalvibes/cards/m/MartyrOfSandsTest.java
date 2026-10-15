package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AdarkarValkyrie;
import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.r.RimewindCryomancer;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyrOfSands.class, AdarkarValkyrie.class, FieldMarshal.class, RimewindCryomancer.class})
class MartyrOfSandsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals two white cards, gains six life, and sacrifices itself")
    void revealsWhiteCardsAndGainsThreeTimesXLife() {
        Card firstWhiteCard = new AdarkarValkyrie();
        Card secondWhiteCard = new FieldMarshal();
        Card nonWhiteCard = new RimewindCryomancer();
        harness.setHand(player1, List.of(firstWhiteCard, secondWhiteCard, nonWhiteCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstWhiteCard.getId(), secondWhiteCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstWhiteCard.getId(), secondWhiteCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstWhiteCard, secondWhiteCard, nonWhiteCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
    }

    @Test
    @DisplayName("Rejects a nonwhite card in the reveal selection")
    void rejectsNonWhiteCardInRevealSelection() {
        Card whiteCard = new AdarkarValkyrie();
        Card nonWhiteCard = new RimewindCryomancer();
        harness.setHand(player1, List.of(whiteCard, nonWhiteCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonWhiteCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card ID");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of(whiteCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(whiteCard, nonWhiteCard);
    }

    @Test
    @DisplayName("Cannot reveal more white cards than are in hand")
    void cannotRevealMoreWhiteCardsThanInHand() {
        Card nonWhiteCard = new RimewindCryomancer();
        harness.setHand(player1, List.of(nonWhiteCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can reveal zero white cards, gain no life, and sacrifice itself")
    void canRevealZeroWhiteCards() {
        Card nonWhiteCard = new RimewindCryomancer();
        harness.setHand(player1, List.of(nonWhiteCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonWhiteCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Pays reveal and sacrifice costs before resolution even while tapped and summoning sick")
    void paysCostsBeforeResolutionAndKeepsChosenX() {
        Card whiteCard = new AdarkarValkyrie();
        Card otherWhiteCard = new FieldMarshal();
        harness.setHand(player1, List.of(whiteCard, otherWhiteCard));
        Permanent martyr = harness.addToBattlefieldAndReturn(player1, new MartyrOfSands());
        martyr.setSummoningSick(true);
        martyr.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(whiteCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(whiteCard, otherWhiteCard);

        harness.setHand(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot reveal the same card twice or fewer cards than the chosen X")
    void rejectsDuplicateAndIncorrectRevealCounts() {
        Card firstWhiteCard = new AdarkarValkyrie();
        Card secondWhiteCard = new FieldMarshal();
        harness.setHand(player1, List.of(firstWhiteCard, secondWhiteCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstWhiteCard.getId(), firstWhiteCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(firstWhiteCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, 20);

        harness.handleMultipleCardsChosen(player1, List.of(firstWhiteCard.getId(), secondWhiteCard.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Can activate with an empty hand by choosing zero")
    void canActivateWithEmptyHand() {
        harness.setHand(player1, List.of());
        Permanent martyr = addCreatureReady(player1, new MartyrOfSands());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
