package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AjaniMentorOfHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("First +1 distributes three counters among controlled creatures")
    void firstPlusOneDistributesCountersAmongControlledCreatures() {
        Permanent ajani = addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.ensurePriority(player1);
        harness.getGameService().activateAbility(
                gd, player1, 0, 0, null, null, null,
                List.of(first.getId(), second.getId()),
                Map.of(first.getId(), 2, second.getId(), 1));
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("First +1 rejects an opponent's creature")
    void firstPlusOneRejectsOpponentsCreature() {
        addReadyAjani(4);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> harness.getGameService().activateAbility(
                gd, player1, 0, 0, null, null, null,
                List.of(opponentCreature.getId()),
                Map.of(opponentCreature.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second +1 offers only Aura, creature, and planeswalker cards")
    void secondPlusOneOffersMatchingCardTypes() {
        Permanent ajani = addReadyAjani(4);
        Card aura = new Pacifism();
        Card creature = new GrizzlyBears();
        Card planeswalker = new ChandraNalaar();
        Card enchantment = new AjanisMantra();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(aura, creature, planeswalker, enchantment));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                aura.getId(), creature.getId(), planeswalker.getId());
        assertThat(choice.validCardIds()).doesNotContain(enchantment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Ultimate gains 100 life")
    void ultimateGainsOneHundredLife() {
        Permanent ajani = addReadyAjani(8);
        gd.playerLifeTotals.put(player1.getId(), 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(107);
    }

    private Permanent addReadyAjani(int loyalty) {
        Permanent permanent = new Permanent(new AjaniMentorOfHeroes());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
