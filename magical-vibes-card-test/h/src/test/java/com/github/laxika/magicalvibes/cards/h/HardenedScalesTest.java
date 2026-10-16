package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CorpsejackMenace;
import com.github.laxika.magicalvibes.cards.p.Pentavus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StranglerootGeist;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HardenedScales.class, GrizzlyBears.class, TimberlandGuide.class, Pentavus.class,
        CorpsejackMenace.class, StranglerootGeist.class, Shock.class})
class HardenedScalesTest extends BaseCardTest {

    @Test
    @DisplayName("adds one +1/+1 counter to counters put on a creature you control")
    void addsOneCounterOnControlledCreature() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("does not add a counter to a creature an opponent controls")
    void doesNotAddOnOpponentCreature() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("multiple Hardened Scales add one counter each")
    void multipleScalesStack() {
        harness.addToBattlefield(player1, new HardenedScales());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("adds one counter to a creature entering with +1/+1 counters")
    void addsOneToEnterWithCounters() {
        harness.addToBattlefield(player1, new HardenedScales());

        harness.setHand(player1, List.of(new Pentavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent pentavus = findPermanent(player1, "Pentavus");
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("adds a counter separately for each counter placement event")
    void addsOneForEachSeparateEvent() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimberlandGuide(), new TimberlandGuide()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castCreature(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("does not modify an opponent's creature entering with counters")
    void doesNotModifyOpponentEnteringCreature() {
        harness.addToBattlefield(player2, new HardenedScales());
        harness.setHand(player1, List.of(new Pentavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pentavus").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
    }

    @Test
    @DisplayName("applies when an opponent puts counters on your creature")
    void appliesToCountersPlacedByOpponent() {
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TimberlandGuide()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("offers the affected creature's controller a choice of replacement order")
    void controllerChoosesOrderWithCounterDoubler() {
        harness.addToBattlefield(player1, new CorpsejackMenace());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new Pentavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(findPermanents(player1, "Pentavus")).isEmpty();
    }

    @Test
    @DisplayName("applying Hardened Scales before the doubler makes the creature enter with twelve counters")
    void scalesThenDoublerEntersWithTwelve() {
        castPentavusWithScalesAndCorpsejack();

        chooseReplacement("Hardened Scales");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Pentavus").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(12);
    }

    @Test
    @DisplayName("applying the doubler before Hardened Scales makes the creature enter with eleven counters")
    void doublerThenScalesEntersWithEleven() {
        castPentavusWithScalesAndCorpsejack();

        chooseReplacement("Corpsejack Menace");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Pentavus").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(11);
    }

    @Test
    @DisplayName("the creature enters and can be used normally once the order is chosen")
    void creatureIsUsableAfterOrderChosen() {
        castPentavusWithScalesAndCorpsejack();
        chooseReplacement("Hardened Scales");

        Permanent pentavus = findPermanent(player1, "Pentavus");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pentavus),
                0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pentavite")).hasSize(1);
        assertThat(pentavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(11);
    }

    @Test
    @DisplayName("offers the replacement order for a creature returned to the battlefield with a counter")
    void controllerChoosesOrderForUndyingReturn() {
        harness.addToBattlefield(player1, new CorpsejackMenace());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(findPermanents(player1, "Strangleroot Geist")).isEmpty();

        chooseReplacement("Hardened Scales");

        assertThat(findPermanent(player1, "Strangleroot Geist").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(4);
    }

    @Test
    @DisplayName("applying the doubler first to an undying return gives three counters")
    void doublerFirstForUndyingReturn() {
        harness.addToBattlefield(player1, new CorpsejackMenace());
        harness.addToBattlefield(player1, new HardenedScales());
        Permanent geist = harness.addToBattlefieldAndReturn(player1, new StranglerootGeist());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, geist.getId());
        harness.passBothPriorities();
        chooseReplacement("Corpsejack Menace");

        assertThat(findPermanent(player1, "Strangleroot Geist").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    private void castPentavusWithScalesAndCorpsejack() {
        harness.addToBattlefield(player1, new CorpsejackMenace());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.setHand(player1, List.of(new Pentavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseReplacement(String sourceName) {
        var choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.startsWith(sourceName))
                .findFirst().orElseThrow());
    }
}
