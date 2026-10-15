package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({MoltenCoreMaestro.class, GrizzlyBears.class, Hurricane.class, Shock.class})
class MoltenCoreMaestroTest extends BaseCardTest {

    private Permanent addMaestro(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MoltenCoreMaestro());
    }

    private void setUpMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    @DisplayName("Casting a cheap instant adds a +1/+1 counter but no mana")
    void cheapSpellAddsCounterOnly() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Cast spent the single red mana; no mana was added by the trigger.
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Casting a four-mana spell adds a counter but no mana")
    void fourManaSpellAddsCounterOnly() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Casting a five-mana spell adds a counter and red mana equal to power (including the new counter)")
    void fiveManaSpellAddsCounterAndMana() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // Base power 2 + the freshly-added +1/+1 counter = 3 red mana.
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Opus")
    void opponentSpellDoesNotTrigger() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Repeated casts add counters and a qualifying cast uses the accumulated power")
    void repeatedCastsUseGrowingPower() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Shock(), new Hurricane()));

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveSorcery(player1, 0, 4);

        assertThat(maestro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing Maestro in response still adds mana using its last battlefield power")
    void removedSourceStillAddsMana() {
        Permanent maestro = addMaestro(player1);
        setUpMainPhase(player1);
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shock()));

        harness.castAndResolveInstant(player2, 0, maestro.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(maestro);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }
}
