package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HumanTimeLordMetaCrisis.class, GrizzlyBears.class, Panopticon.class, ParallelLives.class,
        QueenAllenalOfRuadach.class})
class HumanTimeLordMetaCrisisTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlanechase() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void copiesFirstChoiceAndAddsCountersFromSecondChoicePower() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, legendaryBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.planechase.deck.addFirst(new HumanTimeLordMetaCrisis());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(findPermanents(player2, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.planechase.faceUp).singleElement().extracting(PlanarObject::getCard)
                .isNotInstanceOf(HumanTimeLordMetaCrisis.class);
    }

    @Test
    void oneChosenCreatureCreatesAnUnboostedCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.planechase.deck.addFirst(new HumanTimeLordMetaCrisis());

        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();

        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
    }

    @Test
    void asksTheCreatingPlayerToOrderTokenReplacementsBeforeTheCopyIsCreated() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        harness.addToBattlefield(player2, new GrizzlyBears());
        encounterAndChoose(first, second);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(tokensOf(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    void queenFirstDoublesCopyAndSoldierAndEveryTokenGetsTheCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        harness.addToBattlefield(player2, new GrizzlyBears());
        encounterAndChoose(first, second);

        chooseReplacement(player1, "Queen Allenal of Ruadach");

        assertThat(tokensOf(player1, "Grizzly Bears")).hasSize(2)
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(tokensOf(player1, "Soldier")).hasSize(2)
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(tokensOf(player2, "Grizzly Bears")).singleElement()
                .matches(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0);
    }

    @Test
    void parallelLivesFirstAddsOneUndoubledSoldierThatGetsTheCountersToo() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        harness.addToBattlefield(player2, new GrizzlyBears());
        encounterAndChoose(first, second);

        chooseReplacement(player1, "Parallel Lives");

        assertThat(tokensOf(player1, "Grizzly Bears")).hasSize(2)
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(tokensOf(player1, "Soldier")).singleElement()
                .matches(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    @Test
    void nextPlayerOrdersTheirOwnCreationAndCountersWaitUntilEveryTokenHasEntered() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());
        Permanent opponentFirst = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentSecond = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player2, new ParallelLives());
        encounterAndChoose(first, second);
        harness.handleMultiplePermanentsChosen(player2, List.of(opponentFirst.getId(), opponentSecond.getId()));

        chooseReplacement(player1, "Queen Allenal of Ruadach");

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(tokensOf(player1, "Grizzly Bears")).hasSize(2)
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0);
        assertThat(tokensOf(player2, "Grizzly Bears")).isEmpty();

        chooseReplacement(player2, "Parallel Lives");

        assertThat(tokensOf(player1, "Grizzly Bears")).hasSize(2)
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(tokensOf(player1, "Soldier")).hasSize(2)
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(tokensOf(player2, "Grizzly Bears")).hasSize(2)
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
        assertThat(tokensOf(player2, "Soldier")).singleElement()
                .matches(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    private void encounterAndChoose(Permanent first, Permanent second) {
        gd.planechase.deck.addFirst(new HumanTimeLordMetaCrisis());
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
    }

    private List<Permanent> tokensOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken() && permanent.getCard().getName().equals(name))
                .toList();
    }

    private void chooseReplacement(Player player, String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player.getId());
        harness.handleListChoice(player, choice.options().stream()
                .filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow());
    }

    private GrizzlyBears legendaryBears() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return bears;
    }
}
