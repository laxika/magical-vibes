package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaTheLastHope;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfLiliana.class, GrizzlyBears.class, GiantSpider.class, LilianaTheLastHope.class})
class OathOfLilianaTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, each opponent sacrifices a creature of their choice")
    void eachOpponentSacrificesCreatureOnEnter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new OathOfLiliana()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Giant Spider"));

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("At each end step, creates a Zombie if a planeswalker entered under its controller's control this turn")
    void createsZombieAfterPlaneswalkerEnters() {
        harness.addToBattlefield(player1, new OathOfLiliana());
        harness.setHand(player1, List.of(new LilianaTheLastHope()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not create a Zombie when only a creature entered under its controller's control")
    void doesNotCreateZombieForCreatureEntry() {
        harness.addToBattlefield(player1, new OathOfLiliana());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void doesNotSacrificeControllersCreatureWhenOpponentHasNone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LilianaTheLastHope());
        harness.setHand(player1, List.of(new OathOfLiliana()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Liliana, the Last Hope");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerForOpponentsPlaneswalker() {
        harness.addToBattlefield(player1, new OathOfLiliana());
        harness.enterBattlefieldAndReturn(player2, new LilianaTheLastHope());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    void createsZombieDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new OathOfLiliana());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new LilianaTheLastHope());

        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    void createsZombieWhenPlaneswalkerEnteredBeforeOathAndHasLeftBattlefield() {
        Permanent liliana = harness.enterBattlefieldAndReturn(player1, new LilianaTheLastHope());
        liliana.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Liliana, the Last Hope");
        harness.setHand(player1, List.of(new OathOfLiliana()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    void multiplePlaneswalkerEntriesCreateOnlyOneZombie() {
        harness.addToBattlefield(player1, new OathOfLiliana());
        Permanent firstLiliana = harness.enterBattlefieldAndReturn(player1, new LilianaTheLastHope());
        firstLiliana.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.enterBattlefieldAndReturn(player1, new LilianaTheLastHope());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
