package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnimalAttendant.class, GrizzlyBears.class, FugitiveWizard.class})
class AnimalAttendantTest extends BaseCardTest {

    @Test
    void nonHumanCreatureSpellEntersWithAnAdditionalCounterWhenPaidWithItsMana() {
        addCreatureReady(player1, new AnimalAttendant());
        harness.addMana(player1, ManaColor.BLUE, 1);
        activateAnimalAttendant();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void HumanCreatureSpellDoesNotGetAnAdditionalCounter() {
        addCreatureReady(player1, new AnimalAttendant());
        activateAnimalAttendant(ManaColor.BLUE);

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void CreatureWithoutAnimalAttendantManaDoesNotGetAnAdditionalCounter() {
        addCreatureReady(player1, new AnimalAttendant());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manaFromTwoAttendantsGrantsTwoAdditionalCounters() {
        addCreatureReady(player1, new AnimalAttendant());
        activateAnimalAttendant();
        addCreatureReady(player1, new AnimalAttendant());
        activateAnimalAttendant();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void manaSpentOnAHumanDoesNotGrantCountersToALaterCreature() {
        addCreatureReady(player1, new AnimalAttendant());
        activateAnimalAttendant(ManaColor.BLUE);
        harness.setHand(player1, List.of(new FugitiveWizard(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fugitive Wizard").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
        assertThat(findPermanent(player1, "Grizzly Bears").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void producesOneManaOfTheChosenColorAndTaps(ManaColor color) {
        Permanent attendant = addCreatureReady(player1, new AnimalAttendant());

        activateAnimalAttendant(color);

        assertThat(attendant.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void activateAnimalAttendant() {
        activateAnimalAttendant(ManaColor.GREEN);
    }

    private void activateAnimalAttendant(ManaColor color) {
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, sourceIndex, null, null);
        harness.handleListChoice(player1, color.name());
    }
}
