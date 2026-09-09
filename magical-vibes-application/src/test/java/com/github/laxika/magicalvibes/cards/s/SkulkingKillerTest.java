package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkulkingKiller.class, GrizzlyBears.class, HillGiant.class})
class SkulkingKillerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives the targeted creature -2/-2 when its controller has no other creatures")
    void weakensTargetWhenItIsItsControllersOnlyCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castSkulkingKiller(target);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers even when the targeted creature's controller has another creature")
    void triggersWithAnotherCreatureButDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSkulkingKiller(target);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature entering before resolution prevents the -2/-2 effect")
    void checksForOtherCreaturesAsItResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new SkulkingKiller()));
        addSkulkingKillerMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    private void castSkulkingKiller(Permanent target) {
        harness.setHand(player1, List.of(new SkulkingKiller()));
        addSkulkingKillerMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addSkulkingKillerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
