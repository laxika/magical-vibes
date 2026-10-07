package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
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

@CardUsed({SpectacularSkywhale.class, Shock.class, Hurricane.class, GrizzlyBears.class})
class SpectacularSkywhaleTest extends BaseCardTest {

    private Permanent addSkywhale(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SpectacularSkywhale());
        perm.setSummoningSick(false);
        return perm;
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Casting a cheap instant grants +3/+0 until end of turn and no counters")
    void cheapSpellBoostsWithoutCounters() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isEqualTo(3);
        assertThat(skywhale.getToughnessModifier()).isZero();
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a four-mana spell grants +3/+0 and no counters (below threshold)")
    void fourManaSpellBoostsWithoutCounters() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isEqualTo(3);
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting a five-mana spell puts three +1/+1 counters instead of boosting")
    void fiveManaSpellAddsThreeCounters() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(skywhale.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isZero();
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Opus")
    void opponentSpellDoesNotTrigger() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player2);

        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isZero();
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each instant cast gives its own boost, which expires after the turn")
    void repeatedCheapSpellsStackUntilEndOfTurn() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(skywhale.getPowerModifier()).isEqualTo(6);
        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(skywhale.getPowerModifier()).isZero();
        assertThat(skywhale.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The counters resolve before the expensive spell and remain after the turn")
    void countersPersistAndProtectAgainstTriggeringHurricane() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(skywhale.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skywhale);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(skywhale.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A pending trigger cannot boost a new Skywhale after its source leaves")
    void triggerDoesNotAffectReplacementPermanent() {
        Permanent original = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(original);
        Permanent replacement = addSkywhale(player1);
        harness.passBothPriorities();

        assertThat(replacement.getPowerModifier()).isZero();
        assertThat(replacement.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("More than five mana gives only three counters; a later cheap spell still boosts")
    void manaThresholdUsesEachSpellRatherThanTurnTotal() {
        Permanent skywhale = addSkywhale(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player1, List.of(new Hurricane(), new Shock()));
        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(skywhale.getPowerModifier()).isZero();

        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(skywhale.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(skywhale.getPowerModifier()).isEqualTo(3);
        assertThat(skywhale.getToughnessModifier()).isZero();
    }
}
