package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattleOfHooverDam.class, GiantSpider.class, GrizzlyBears.class, Naturalize.class, Shock.class})
class BattleOfHooverDamTest extends BaseCardTest {

    @Test
    void ncrReturnsTargetCreatureWithManaValueThreeOrLessAndAFinalityCounter() {
        Card eligible = new GrizzlyBears();
        Card ineligible = new GiantSpider();
        harness.setGraveyard(player1, List.of(eligible, ineligible));
        castAndChoose("NCR");

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(ineligible.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    void legionPutsTwoPlusOneCountersOnTargetCreatureWhenYourCreatureDies() {
        castAndChoose("Legion");
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void ncrTriggerStillReturnsCreatureAfterEnchantmentIsDestroyed() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castAndChoose("NCR");
        Permanent battle = findPermanent(player1, "Battle of Hoover Dam");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, battle.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Battle of Hoover Dam");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    void ncrDoesNotTriggerDuringOpponentsEndStep() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndChoose("NCR");

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void legionDoesNotReturnCreaturesAtEndStep() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castAndChoose("Legion");

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void ncrDoesNotPutCountersOnCreaturesWhenYourCreatureDies() {
        castAndChoose("NCR");
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void legionDoesNotTriggerWhenOpponentsCreatureDies() {
        castAndChoose("Legion");
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void legionCannotTargetOpponentsCreature() {
        castAndChoose("Legion");
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void returnedCreatureIsExiledInsteadOfDyingAndDoesNotTriggerLegion() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        castAndChoose("NCR");
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castAndChoose("Legion");
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void castAndChoose(String mode) {
        harness.setHand(player1, List.of(new BattleOfHooverDam()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
