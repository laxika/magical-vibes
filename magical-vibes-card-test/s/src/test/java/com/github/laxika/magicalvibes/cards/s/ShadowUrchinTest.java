package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowUrchin.class, GrizzlyBears.class, Island.class, Shock.class})
class ShadowUrchinTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, it blights a creature you control")
    @CardUsed({ShadowUrchin.class})
    void attacksBlightsAControlledCreature() {
        Permanent urchin = addCreatureReady(player1, new ShadowUrchin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(urchin.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @CardUsed({ShadowUrchin.class})
    void attackAllowsChoosingAnotherControlledCreatureToBlight() {
        Permanent attacker = addCreatureReady(player1, new ShadowUrchin());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new ShadowUrchin());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ShadowUrchin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(chosen.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("When a countered creature you control dies, exiles one card per counter until your next end step")
    void counteredAllyDeathExilesCardsUntilNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ShadowUrchin());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 2);

        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId()).doesNotContainKey(second.getId());
    }

    @Test
    @DisplayName("Does not trigger when the dying creature has no counters")
    void counterlessAllyDeathDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ShadowUrchin());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dying.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
    }

    @Test
    void ownDeathWithCountersExilesCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent urchin = harness.addToBattlefieldAndReturn(player1, new ShadowUrchin());
        urchin.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, urchin.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void permissionEndsWhenNextEndStepBegins() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addToBattlefield(player1, new ShadowUrchin());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 1);
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dying.getId());
        resolveAllTriggers();
        assertThat(gd.exilePlayPermissions).containsKey(top.getId());

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    void countsDifferentCounterTypesAndAllowsPlayingAnExiledLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ShadowUrchin());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 1);
        dying.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, dying.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void opposingCreatureDeathDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new ShadowUrchin());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 2);
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, dying.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(top.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
