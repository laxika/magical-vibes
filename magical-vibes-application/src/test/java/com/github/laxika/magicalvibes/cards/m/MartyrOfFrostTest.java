package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.r.RimewindCryomancer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MartyrOfFrost.class, RimewindCryomancer.class, BorealDruid.class})
class MartyrOfFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals blue cards, counters an unpaid spell, and sacrifices itself")
    void countersWhenOpponentCannotPay() {
        RimewindCryomancer firstBlueCard = new RimewindCryomancer();
        RimewindCryomancer secondBlueCard = new RimewindCryomancer();
        BorealDruid nonBlueCard = new BorealDruid();
        harness.setHand(player1, List.of(firstBlueCard, secondBlueCard, nonBlueCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfFrost());

        harness.forceActivePlayer(player2);
        BorealDruid druid = new BorealDruid();
        harness.setHand(player2, List.of(druid));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, druid.getId());

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstBlueCard.getId(), secondBlueCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstBlueCard.getId(), secondBlueCard.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Boreal Druid");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstBlueCard, secondBlueCard, nonBlueCard);
    }

    @Test
    @DisplayName("Opponent can pay X and the targeted spell resolves")
    void leavesSpellUncounteredWhenOpponentPays() {
        RimewindCryomancer blueCard = new RimewindCryomancer();
        harness.setHand(player1, List.of(blueCard));
        Permanent martyr = addCreatureReady(player1, new MartyrOfFrost());

        harness.forceActivePlayer(player2);
        BorealDruid druid = new BorealDruid();
        harness.setHand(player2, List.of(druid));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, druid.getId());
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boreal Druid");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
    }

    @Test
    @DisplayName("Cannot activate without enough blue cards to reveal")
    void cannotRevealMoreBlueCardsThanAreInHand() {
        harness.setHand(player1, List.of(new BorealDruid()));
        Permanent martyr = addCreatureReady(player1, new MartyrOfFrost());
        harness.forceActivePlayer(player2);
        BorealDruid druid = new BorealDruid();
        harness.setHand(player2, List.of(druid));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, druid.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(martyr);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can reveal zero blue cards and does not counter the targeted spell")
    void canRevealZeroBlueCards() {
        Permanent martyr = addCreatureReady(player1, new MartyrOfFrost());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        BorealDruid druid = new BorealDruid();
        harness.setHand(player2, List.of(druid));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, druid.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boreal Druid");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(martyr);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(martyr.getCard());
    }

    @Test
    @DisplayName("Counters the spell when its controller declines an affordable payment")
    void countersWhenOpponentDeclinesPayment() {
        RimewindCryomancer blueCard = new RimewindCryomancer();
        harness.setHand(player1, List.of(blueCard));
        addCreatureReady(player1, new MartyrOfFrost());
        harness.forceActivePlayer(player2);
        BorealDruid druid = new BorealDruid();
        harness.setHand(player2, List.of(druid));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, druid.getId());
        harness.handleMultipleCardsChosen(player1, List.of(blueCard.getId()));
        harness.assertInGraveyard(player1, "Martyr of Frost");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Boreal Druid");
        harness.assertNotOnBattlefield(player2, "Boreal Druid");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Revealing only one of two blue cards requires a payment of only one mana")
    void canRevealFewerThanAllBlueCards() {
        RimewindCryomancer revealed = new RimewindCryomancer();
        RimewindCryomancer unrevealed = new RimewindCryomancer();
        harness.setHand(player1, List.of(revealed, unrevealed));
        addCreatureReady(player1, new MartyrOfFrost());
        harness.forceActivePlayer(player2);
        BorealDruid druid = new BorealDruid();
        harness.setHand(player2, List.of(druid));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, druid.getId());
        harness.handleMultipleCardsChosen(player1, List.of(revealed.getId()));
        harness.assertInGraveyard(player1, "Martyr of Frost");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Boreal Druid");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed, unrevealed);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }
}
