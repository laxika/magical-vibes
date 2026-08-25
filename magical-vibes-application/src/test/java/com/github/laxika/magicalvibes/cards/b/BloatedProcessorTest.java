package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianBroodlings;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloatedProcessor.class, PhyrexianBroodlings.class, GrizzlyBears.class, FlameJavelin.class})
class BloatedProcessorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Phyrexian puts a +1/+1 counter on Bloated Processor")
    void sacrificingAnotherPhyrexianPutsCounterOnIt() {
        Permanent processor = harness.addToBattlefieldAndReturn(player1, new BloatedProcessor());
        harness.addToBattlefield(player1, new PhyrexianBroodlings());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(processor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Phyrexian Broodlings");
    }

    @Test
    @DisplayName("It cannot sacrifice a non-Phyrexian for its ability")
    void cannotSacrificeNonPhyrexian() {
        harness.addToBattlefield(player1, new BloatedProcessor());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When it dies, it incubates X where X is its power")
    void deathIncubatesItsPower() {
        Permanent processor = harness.addToBattlefieldAndReturn(player1, new BloatedProcessor());
        addCounterBySacrificingPhyrexian(processor);

        killWithFlameJavelin(processor);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void addCounterBySacrificingPhyrexian(Permanent processor) {
        harness.addToBattlefield(player1, new PhyrexianBroodlings());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(processor), null, null);
        harness.passBothPriorities();
    }

    private void killWithFlameJavelin(Permanent processor) {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, java.util.List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castInstant(player2, 0, processor.getId());
        harness.passBothPriorities();
    }
}
