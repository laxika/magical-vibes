package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VorelOfTheHullCladeTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles +1/+1 counters on a target creature and taps the source")
    void doublesCountersOnCreature() {
        Permanent vorel = addVorel(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(bears.getEffectivePower()).isEqualTo(8);
        assertThat(vorel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Doubles every kind of counter on the target")
    void doublesEachKindOfCounter() {
        addVorel(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Can target an artifact an opponent controls")
    void doublesCountersOnOpponentArtifact() {
        addVorel(player1);
        Permanent artifact = addTyped(player2, CardType.ARTIFACT, "Test Artifact");
        artifact.setCounterCount(CounterType.CHARGE, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target an enchantment")
    void cannotTargetEnchantment() {
        addVorel(player1);
        Permanent enchantment = addTyped(player1, CardType.ENCHANTMENT, "Test Enchantment");
        enchantment.setCounterCount(CounterType.CHARGE, 2);
        prepareTurn();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(enchantment.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does nothing when the target has no counters")
    void noOpWithoutCounters() {
        addVorel(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent addVorel(Player player) {
        Permanent perm = new Permanent(new VorelOfTheHullClade());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTyped(Player player, CardType type, String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
