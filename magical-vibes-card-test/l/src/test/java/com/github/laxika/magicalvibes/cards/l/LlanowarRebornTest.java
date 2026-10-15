package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LlanowarReborn.class, GrizzlyBears.class, Tatterkite.class})
class LlanowarRebornTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with a +1/+1 counter")
    void entersTappedWithCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LlanowarReborn()));

        harness.playLand(player1, 0);

        Permanent reborn = findPermanent(player1, "Llanowar Reborn");
        assertThat(reborn.isTapped()).isTrue();
        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds one green mana")
    void tapsForGreenMana() {
        Permanent reborn = harness.addToBattlefieldAndReturn(player1, new LlanowarReborn());
        reborn.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(reborn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its controller may move its counter onto a creature that enters")
    void controllerMayMoveCounterOntoEnteringCreature() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining graft leaves the counter on the land")
    void decliningGraftLeavesCounterOnLand() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft does not trigger without a +1/+1 counter")
    void graftDoesNotTriggerWithoutCounter() {
        Permanent reborn = addRebornWithCounter();
        reborn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        castCreature(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft does not trigger when a land enters")
    void graftDoesNotTriggerForNoncreatureEntry() {
        Permanent reborn = addRebornWithCounter();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LlanowarReborn()));
        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The land's controller chooses for an opponent's creature")
    void controllerChoosesForOpponentsCreature() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft cannot move a counter removed before resolution")
    void counterRemovedBeforeResolution() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player1);
        harness.handleMayAbilityChosen(player1, true);

        reborn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Graft keeps the source counter when the entering creature has left")
    void enteringCreatureLeavesBeforeResolution() {
        Permanent reborn = addRebornWithCounter();
        castCreature(player1);
        harness.handleMayAbilityChosen(player1, true);

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerHands.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Graft moves only one counter even when the land has several")
    void movesOnlyOneCounter() {
        Permanent reborn = addRebornWithCounter();
        reborn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        castCreature(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graft keeps its counter when the entering creature cannot receive counters")
    void cannotMoveCounterOntoTatterkite() {
        Permanent reborn = addRebornWithCounter();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Tatterkite(), "{3}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(reborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Tatterkite")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addRebornWithCounter() {
        return harness.enterBattlefieldAndReturn(player1, new LlanowarReborn());
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
