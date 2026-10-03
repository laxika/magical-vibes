package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Biophagus.class, GrizzlyBears.class, FugitiveWizard.class, SolRing.class})
class BiophagusTest extends BaseCardTest {

    @Test
    void creatureSpellEntersWithAnAdditionalCounterWhenPaidWithItsMana() {
        addCreatureReady(player1, new Biophagus());
        harness.addMana(player1, ManaColor.BLUE, 1);
        activateBiophagus();

        harness.castFromHand(player1, new GrizzlyBears(), "");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void humanCreatureSpellAlsoGetsAnAdditionalCounter() {
        addCreatureReady(player1, new Biophagus());
        activateBiophagus(ManaColor.BLUE);

        harness.castFromHand(player1, new FugitiveWizard(), "");
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureWithoutBiophagusManaDoesNotGetAnAdditionalCounter() {
        addCreatureReady(player1, new Biophagus());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFromHand(player1, new GrizzlyBears(), "");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manaFromTwoBiophagusesAddsTwoCountersToOneCreature() {
        addCreatureReady(player1, new Biophagus());
        activateBiophagus();
        addCreatureReady(player1, new Biophagus());
        activateBiophagus();

        Biophagus creature = new Biophagus();
        harness.castFromHand(player1, creature, "");
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void manaSpentOnANoncreatureDoesNotGrantCountersOrCarryOverToALaterCreature() {
        addCreatureReady(player1, new Biophagus());
        activateBiophagus(ManaColor.RED);
        harness.castFromHand(player1, new SolRing(), "");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sol Ring").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Biophagus creature = new Biophagus();
        harness.castFromHand(player1, creature, "{1}{G}");
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesOneManaOfTheChosenColorImmediatelyAndTapsTheSource(ManaColor color) {
        Permanent source = addCreatureReady(player1, new Biophagus());

        activateBiophagus(color);

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private void activateBiophagus() {
        activateBiophagus(ManaColor.GREEN);
    }

    private void activateBiophagus(ManaColor color) {
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, sourceIndex, null, null);
        harness.handleListChoice(player1, color.name());
    }
}
