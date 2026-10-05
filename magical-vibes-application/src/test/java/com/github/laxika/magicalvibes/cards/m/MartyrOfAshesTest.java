package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorealGriffin;
import com.github.laxika.magicalvibes.cards.b.BalduvianRage;
import com.github.laxika.magicalvibes.cards.r.RiteOfFlame;
import com.github.laxika.magicalvibes.cards.r.RonomHulk;
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

@CardUsed({MartyrOfAshes.class, BorealGriffin.class, BalduvianRage.class, RiteOfFlame.class,
        RonomHulk.class})
class MartyrOfAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals red cards, sacrifices itself, and damages each creature without flying")
    void revealsRedCardsAndDamagesNonFlyers() {
        RiteOfFlame firstRedCard = new RiteOfFlame();
        BalduvianRage secondRedCard = new BalduvianRage();
        harness.setHand(player1, List.of(firstRedCard, secondRedCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfAshes());
        Permanent ownNonFlyer = addCreatureReady(player1, new RonomHulk());
        Permanent opposingNonFlyer = addCreatureReady(player2, new RonomHulk());
        Permanent opposingFlyer = addCreatureReady(player2, new BorealGriffin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstRedCard.getId(), secondRedCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstRedCard.getId(), secondRedCard.getId()));
        harness.passBothPriorities();

        assertThat(ownNonFlyer.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingNonFlyer.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingFlyer.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstRedCard, secondRedCard);
    }

    @Test
    @DisplayName("Cannot reveal more red cards than are in hand")
    void cannotRevealMoreRedCardsThanAreInHand() {
        RiteOfFlame redCard = new RiteOfFlame();
        harness.setHand(player1, List.of(redCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfAshes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(redCard);
    }

    @Test
    @DisplayName("Only red cards can be selected for the reveal cost")
    void onlyRedCardsCanBeSelectedForRevealCost() {
        RiteOfFlame redCard = new RiteOfFlame();
        BorealGriffin nonRedCard = new BorealGriffin();
        harness.setHand(player1, List.of(redCard, nonRedCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfAshes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(redCard.getId());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonRedCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card ID");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(redCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(redCard, nonRedCard);
    }

    @Test
    @DisplayName("Can reveal zero red cards, sacrifice itself, and deal no damage")
    void canRevealZeroRedCards() {
        BorealGriffin nonRedCard = new BorealGriffin();
        harness.setHand(player1, List.of(nonRedCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfAshes());
        Permanent nonFlyer = addCreatureReady(player2, new RonomHulk());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(nonFlyer.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonRedCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Reveal cost requires exactly X distinct red cards")
    void requiresExactlyXDistinctCards() {
        RiteOfFlame first = new RiteOfFlame();
        BalduvianRage second = new BalduvianRage();
        harness.setHand(player1, List.of(first, second));
        Permanent martyr = harness.addToBattlefieldAndReturn(player1, new MartyrOfAshes());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Martyr of Ashes");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("A tapped summoning-sick Martyr can activate and X stays fixed after cards leave hand")
    void activationDoesNotRequireTapAndDamageUsesChosenX() {
        RiteOfFlame redCard = new RiteOfFlame();
        harness.setHand(player1, List.of(redCard));
        Permanent martyr = harness.addToBattlefieldAndReturn(player1, new MartyrOfAshes());
        martyr.setSummoningSick(true);
        martyr.setTapped(true);
        harness.addToBattlefield(player2, new MartyrOfAshes());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new RonomHulk());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null);
        harness.handleMultipleCardsChosen(player1, List.of(redCard.getId()));

        harness.assertNotOnBattlefield(player1, "Martyr of Ashes");
        harness.assertInGraveyard(player1, "Martyr of Ashes");
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(martyr.getCard(), redCard));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Martyr of Ashes");
        harness.assertInGraveyard(player2, "Martyr of Ashes");
        assertThat(survivor.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
