package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrazenCollector.class, BarkformHarvester.class})
class BrazenCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking adds one red mana")
    void attackingAddsRedMana() {
        addCreatureReady(player1, new BrazenCollector());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger Brazen Collector")
    void anotherCreatureAttackingDoesNotTrigger() {
        addCreatureReady(player1, new BrazenCollector());
        addCreatureReady(player1, new BarkformHarvester());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Mana from Brazen Collector survives step transitions but ends with the turn")
    void manaSurvivesStepTransitionsUntilEndOfTurn() {
        addCreatureReady(player1, new BrazenCollector());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.RED, 2);

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Attack mana uses the stack and goes to the attacking controller")
    void attackManaUsesStackAndGoesToController() {
        addCreatureReady(player2, new BrazenCollector());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();

        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Each attacking Collector adds its own red mana")
    void multipleCollectorsEachAddMana() {
        addCreatureReady(player1, new BrazenCollector());
        addCreatureReady(player1, new BrazenCollector());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spending Collector mana does not make later ordinary mana persistent")
    void spendingPersistentManaDoesNotProtectLaterMana() {
        addCreatureReady(player1, new BrazenCollector());
        harness.setHand(player1, List.of(new BrazenCollector()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.castCreature(player1, 0);
            resolveAllTriggers();
        });

        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);

        pool.add(ManaColor.RED, 1);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(pool.get(ManaColor.RED)).isZero();
    }
}
