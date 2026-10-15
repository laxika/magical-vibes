package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.cards.s.SelectForInspection;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherstormRoc.class, ConsulateSkygate.class, SelectForInspection.class})
class AetherstormRocTest extends BaseCardTest {

    @Test
    @DisplayName("Gains an energy counter when it enters and when another creature enters")
    void gainsEnergyFromCreatureEntries() {
        harness.setHand(player1, List.of(new AetherstormRoc()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);

        harness.setHand(player1, List.of(new ConsulateSkygate()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("May pay energy on attack to grow and tap a defending creature")
    void paysEnergyOnAttack() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot pay the attack cost without enough energy")
    void cannotPayWithoutEnoughEnergy() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void mayDeclineEnergyPayment() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void mayPayWithoutChoosingAnAvailableTarget() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());
        gd.playerEnergyCounters.put(player1.getId(), 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void mayPayWhenDefenderControlsNoCreatures() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void oneEnergyCannotPayTheAttackCost() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());
        gd.playerEnergyCounters.put(player1.getId(), 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    void opposingCreatureEnteringDoesNotGrantEnergy() {
        addCreatureReady(player1, new AetherstormRoc());
        harness.setHand(player2, List.of(new ConsulateSkygate()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void cannotTargetAnAttackingPlayersCreature() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent ally = addCreatureReady(player1, new ConsulateSkygate());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        declareAttackers(List.of(0));
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ally.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.isTapped()).isFalse();
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void losingTheChosenTargetPreventsPaymentAndGrowth() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());
        victim.tap();
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(roc.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void attackTriggerStillTapsTargetAfterRocLeavesBattlefield() {
        Permanent roc = addCreatureReady(player1, new AetherstormRoc());
        Permanent victim = addCreatureReady(player2, new ConsulateSkygate());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.castAndResolveInstant(player1, 0, roc.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(roc);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(victim.isTapped()).isTrue();
    }
}
