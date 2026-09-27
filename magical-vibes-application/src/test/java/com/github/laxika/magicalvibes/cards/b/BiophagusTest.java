package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Biophagus.class, GrizzlyBears.class, FugitiveWizard.class})
class BiophagusTest extends BaseCardTest {

    @Test
    void creatureSpellEntersWithAnAdditionalCounterWhenPaidWithItsMana() {
        addCreatureReady(player1, new Biophagus());
        harness.addMana(player1, ManaColor.BLUE, 1);
        activateBiophagus();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void humanCreatureSpellAlsoGetsAnAdditionalCounter() {
        addCreatureReady(player1, new Biophagus());
        activateBiophagus(ManaColor.BLUE);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureWithoutBiophagusManaDoesNotGetAnAdditionalCounter() {
        addCreatureReady(player1, new Biophagus());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
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
