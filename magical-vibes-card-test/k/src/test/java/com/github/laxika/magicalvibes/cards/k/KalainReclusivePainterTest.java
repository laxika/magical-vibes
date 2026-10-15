package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PowerWordKill;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KalainReclusivePainter.class, WilyGoblin.class, FugitiveWizard.class, GrizzlyBears.class,
        PowerWordKill.class})
class KalainReclusivePainterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when Kalain enters")
    void createsTreasureOnEntry() {
        harness.setHand(player1, List.of(new KalainReclusivePainter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("A creature enters with one counter for one Treasure mana spent to cast it")
    void creatureEntersWithCountersForTreasureMana() {
        addReadyKalain(player1);
        createTreasure();
        sacrificeTreasureFor(ManaColor.BLUE);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry counter count equals the number of Treasure mana spent")
    void entryCountersScaleWithTreasureManaSpent() {
        addReadyKalain(player1);
        createTwoTreasures();
        sacrificeTreasureFor(ManaColor.RED);
        sacrificeTreasureFor(ManaColor.GREEN);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering without Treasure mana do not get a counter")
    void noCounterWithoutTreasureMana() {
        addReadyKalain(player1);
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Kalain does not grant the entry counter to an opponent's creature")
    void doesNotAffectOpponentsCreatures() {
        addReadyKalain(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player2, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void mixedManaOnlyCountsManaFromTreasure() {
        addReadyKalain(player1);
        createTreasure();
        sacrificeTreasureFor(ManaColor.GREEN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void kalainDoesNotGiveItselfCounters() {
        createTwoTreasures();
        sacrificeTreasureFor(ManaColor.BLACK);
        sacrificeTreasureFor(ManaColor.RED);
        harness.setHand(player1, List.of(new KalainReclusivePainter()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Kalain, Reclusive Painter")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void opponentSpendingTreasureManaDoesNotReceiveCounters() {
        addReadyKalain(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        createTreasure(player2);
        sacrificeTreasureFor(player2, ManaColor.BLUE);
        harness.setHand(player2, List.of(new FugitiveWizard()));

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player2, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void kalainMustStillBePresentWhenCreatureEnters() {
        Permanent kalain = addReadyKalain(player1);
        createTreasure();
        sacrificeTreasureFor(ManaColor.BLUE);
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.castCreature(player1, 0);

        harness.setHand(player2, List.of(new PowerWordKill()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, kalain.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Kalain, Reclusive Painter");
        assertThat(findPermanent(player1, "Fugitive Wizard")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void uncastCreatureDoesNotUseTreasureManaFloatingInPool() {
        addReadyKalain(player1);
        createTreasure();
        sacrificeTreasureFor(ManaColor.BLUE);

        Permanent wizard = harness.enterBattlefieldAndReturn(player1, new FugitiveWizard());
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyKalain(Player player) {
        return addCreatureReady(player, new KalainReclusivePainter());
    }

    private void createTreasure() {
        createTreasure(player1);
    }

    private void createTreasure(Player player) {
        harness.setHand(player, List.of(new WilyGoblin()));
        harness.addMana(player, ManaColor.RED, 2);
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }

    private void createTwoTreasures() {
        harness.setHand(player1, List.of(new WilyGoblin(), new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void sacrificeTreasureFor(ManaColor color) {
        sacrificeTreasureFor(player1, color);
    }

    private void sacrificeTreasureFor(Player player, ManaColor color) {
        Permanent treasure = findPermanent(player, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player.getId()).indexOf(treasure);
        harness.activateAbility(player, treasureIndex, null, null);
        harness.handleListChoice(player, color.name());
    }
}
