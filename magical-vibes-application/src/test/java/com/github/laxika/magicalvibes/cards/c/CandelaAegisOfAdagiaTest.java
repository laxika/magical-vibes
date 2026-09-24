package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CandelaAegisOfAdagia.class, GrizzlyBears.class})
class CandelaAegisOfAdagiaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a creature you control and its card keeps the perpetual boost")
    void etbReturnsAndPerpetuallyBoostsCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CandelaAegisOfAdagia()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card returnedCard = bears.getCard();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(returnedCard.getId());
        assertThat(gd.perpetualCardPowerToughnessModifiers).containsKey(returnedCard.getId());

        harness.setHand(player1, List.of(returnedCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent recast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == returnedCard)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, recast)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recast)).isEqualTo(3);
    }

    @Test
    @DisplayName("Station animates Candela and grants flying at eight charge counters")
    void stationUnlocksFlying() {
        Permanent candela = harness.addToBattlefieldAndReturn(player1, new CandelaAegisOfAdagia());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(candela), null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(candela.getCounterCount(CounterType.CHARGE)).isEqualTo(2);

        candela.setCounterCount(CounterType.CHARGE, 8);
        assertThat(gqs.isCreature(gd, candela)).isTrue();
        assertThat(gqs.hasKeyword(gd, candela, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Combat damage offers a creature card with mana value three or less")
    void combatDamageOffersEligibleCreature() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard() == bears);
    }

    @Test
    @DisplayName("Declining the combat-damage card choice leaves the card in hand")
    void combatDamageMayBeDeclined() {
        Permanent candela = addCreatureReady(player1, new CandelaAegisOfAdagia());
        candela.setCounterCount(CounterType.CHARGE, 8);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == bears);
    }
}
