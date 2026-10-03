package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NervousGardener;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Candlestick.class, GrizzlyBears.class, NervousGardener.class})
class CandlestickTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping a creature gives it +1/+1 and makes it surveil 2 when it attacks")
    void equippedCreatureBoostsAndSurveilsWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent candlestick = harness.addToBattlefieldAndReturn(player1, new Candlestick());
        int candlestickIndex = gd.playerBattlefields.get(player1.getId()).indexOf(candlestick);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, candlestickIndex, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(candlestick.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Card keptCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard, keptCard));

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Sacrificing Candlestick draws a card")
    void sacrificeAbilityDrawsCard() {
        harness.addToBattlefield(player1, new Candlestick());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        harness.assertInGraveyard(player1, "Candlestick");
    }

    @Test
    @DisplayName("Sacrificing Candlestick pays the cost immediately and preserves an attack trigger")
    void attackTriggerSurvivesSacrificingEquipment() {
        Permanent creature = addCreatureReady(player1, new NervousGardener());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Candlestick());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();

        Card drawn = new Candlestick();
        Card first = new NervousGardener();
        Card second = new Candlestick();
        harness.setLibrary(player1, List.of(drawn, first, second));
        declareAttackers(player1, List.of(0));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
        harness.assertInGraveyard(player1, "Candlestick");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        harness.passBothPriorities();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                List.of(1, 0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("Surveil 2 can put the only card in the library into the graveyard")
    void surveilWithOneCardInLibrary() {
        Permanent creature = addCreatureReady(player1, new NervousGardener());
        harness.addToBattlefield(player1, new Candlestick());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 1, null, creature.getId());
        harness.passBothPriorities();
        Card onlyCard = new Candlestick();
        harness.setLibrary(player1, List.of(onlyCard));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }
}
