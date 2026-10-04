package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GraniteWitness;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FussBother.class, GraniteWitness.class})
class FussBotherTest extends BaseCardTest {

    @Test
    @DisplayName("Fuss puts counters only on your attacking creatures")
    void fussCountersYourAttackingCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        attacker.setAttacking(true);
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        Permanent opponentAttacker = harness.addToBattlefieldAndReturn(player2, new GraniteWitness());
        opponentAttacker.setAttacking(true);

        harness.setHand(player1, List.of(new FussBother()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bother creates Thopters and surveils two")
    void botherCreatesThoptersAndSurveils() {
        Card topCard = new GraniteWitness();
        Card secondCard = new GraniteWitness();
        FussBother bother = new FussBother();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(bother));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(3);
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .startsWith(topCard, secondCard)
                .hasSize(3);
    }

    @Test
    void botherCannotBeCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new FussBother()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fussUsesAttackingCreaturesAtResolution() {
        Permanent formerAttacker = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        formerAttacker.setAttacking(true);
        Permanent newAttacker = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        harness.setHand(player1, List.of(new FussBother()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalInstant(player1, 0, 0, List.of());
        formerAttacker.setAttacking(false);
        newAttacker.setAttacking(true);
        harness.passBothPriorities();

        assertThat(formerAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(newAttacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void botherCanKeepSurveilledCardsInReverseOrder() {
        Card first = new GraniteWitness();
        Card second = new GraniteWitness();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new FussBother()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(findPermanents(player1, "Thopter")).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void botherCreatesTokensWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FussBother()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
