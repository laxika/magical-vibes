package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.cards.v.VitalSurge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PetalmaneBaku.class, VitalSurge.class, TeardropKami.class, GoblinCohort.class})
class PetalmaneBakuTest extends BaseCardTest {

    @Test
    @DisplayName("May put a ki counter on itself when an Arcane spell is cast")
    void arcaneSpellAddsKiCounterWhenAccepted() {
        Permanent baku = addReadyBaku();
        harness.castFromHand(player1, new VitalSurge(), "{1}{G}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("May put a ki counter on itself when a Spirit spell is cast")
    void spiritSpellAddsKiCounterWhenAccepted() {
        Permanent baku = addReadyBaku();
        harness.castFromHand(player1, new TeardropKami(), "{U}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the spell trigger leaves the ki counter off")
    void decliningSpellTriggerAddsNoKiCounter() {
        Permanent baku = addReadyBaku();
        harness.castFromHand(player1, new VitalSurge(), "{1}{G}");

        harness.handleMayAbilityChosen(player1, false);

        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("A spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        Permanent baku = addReadyBaku();
        harness.castFromHand(player1, new GoblinCohort(), "{R}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger")
    void opponentSpiritSpellDoesNotTrigger() {
        Permanent baku = addReadyBaku();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new TeardropKami(), "{U}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Removes the chosen number of ki counters and adds that much mana of the chosen color")
    void removesXCountersForAnyColorMana() {
        Permanent baku = addReadyBaku();
        baku.setCounterCount(CounterType.KI, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(baku.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The mana ability may remove zero ki counters")
    void canActivateWithZeroKiCounters() {
        Permanent baku = addReadyBaku();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue()).isZero();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(baku.getCounterCount(CounterType.KI)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyBaku() {
        return addCreatureReady(player1, new PetalmaneBaku());
    }
}
