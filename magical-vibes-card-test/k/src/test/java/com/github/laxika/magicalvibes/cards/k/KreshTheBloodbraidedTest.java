package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.cards.s.SavageHunger;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KreshTheBloodbraided.class, RuneclawBear.class, Shock.class, DoomBlade.class,
        SavageHunger.class, MagmaSpray.class, DayOfJudgment.class})
class KreshTheBloodbraidedTest extends BaseCardTest {

    private Permanent kresh() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new RuneclawBear());
    }

    @Test
    @DisplayName("Accepting the may puts X +1/+1 counters equal to the dying creature's power")
    void putsCountersEqualToDyingPowerOnAccept() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may puts no counters")
    void noCountersOnDecline() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters equal the dying creature's last-known effective power (with +1/+1 counters)")
    void usesEffectivePowerOfDyingCreature() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // 2/2 Runeclaw Bear becomes 3/3

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature controlled by Kresh's controller also triggers the ability")
    void triggersForOwnCreature() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Last-known power includes an Aura's continuous power bonus")
    void includesAuraBonusInDyingPower() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SavageHunger());
        aura.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiling a creature instead of its death does not trigger Kresh")
    void exileReplacementDoesNotTrigger() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(bears.getCard().getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(kresh().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A trigger cannot put counters on Kresh after Kresh leaves")
    void removedSourceDoesNotReceiveCounters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KreshTheBloodbraided());
        Permanent bears = addCreature(player2);
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kresh dying alone does not trigger its own ability")
    void doesNotTriggerForOwnDeath() {
        harness.addToBattlefield(player1, new KreshTheBloodbraided());
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Kresh sees another creature die simultaneously but cannot receive counters")
    void simultaneousDeathStillTriggersForOtherCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new KreshTheBloodbraided());
        addCreature(player2);
        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
