package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScoldingAdministratorTest extends BaseCardTest {

    // ===== Structure =====

    @Test
    @DisplayName("Repartee puts a +1/+1 counter on itself; death trigger moves its counters")
    void hasCorrectStructure() {
        ScoldingAdministrator card = new ScoldingAdministrator();

        SpellCastTriggerEffect trigger =
                (SpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.castSpellTargetCondition()).isInstanceOf(StackEntryTargetsPermanentPredicate.class);
        assertThat(trigger.resolvedEffects()).singleElement().isInstanceOf(PutCountersOnSelfEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).singleElement()
                .isInstanceOf(MoveDyingSourceCountersToTargetCreatureEffect.class);
    }

    // ===== Repartee: +1/+1 counter on itself =====

    @Test
    @DisplayName("Casting an instant that targets a creature puts a +1/+1 counter on the Administrator")
    void reparteeAddsCounterToSelf() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, giantId);
        harness.passBothPriorities();

        assertThat(admin.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(admin.getPlusOnePlusOneCounters()).isZero();
    }

    // ===== Death trigger: move counters to a target creature =====

    @Test
    @DisplayName("Dying with counters moves those counters to a chosen creature")
    void dyingWithCountersMovesThem() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        admin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        admin.tap(); // so it can be Assassinated
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        UUID adminId = admin.getId();
        gs.playCard(gd, player2, 0, 0, adminId, null);
        harness.passBothPriorities(); // Assassinate resolves — Administrator dies, death trigger asks for a target

        // Choose the Grizzly Bears to receive the counters
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve the counter-move trigger

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Dying with no counters does not trigger (intervening-if fails)")
    void dyingWithoutCountersDoesNotTrigger() {
        Permanent admin = addCreatureReady(player1, new ScoldingAdministrator());
        admin.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, admin.getId(), null);
        harness.passBothPriorities(); // Assassinate resolves — Administrator dies, no trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPlusOnePlusOneCounters()).isZero();
    }
}
