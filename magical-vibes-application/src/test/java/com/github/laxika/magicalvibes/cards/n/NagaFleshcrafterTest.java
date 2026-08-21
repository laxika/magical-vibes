package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NagaFleshcrafter.class, GrizzlyBears.class, HillGiant.class, ThrunTheLastTroll.class})
class NagaFleshcrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters as a copy of a creature on the battlefield")
    void entersAsCopyOfCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NagaFleshcrafter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copy = findPermanent(player1, "Grizzly Bears");
        assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(copy.getCard().getPower()).isEqualTo(2);
        assertThat(copy.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Renew counters the target and copies it to other creatures until end of turn")
    void renewCountersAndCopiesTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new NagaFleshcrafter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(other.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Naga Fleshcrafter");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(other.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("Renew cannot target a legendary creature")
    void renewCannotTargetLegendaryCreature() {
        Permanent legendary = addCreatureReady(player1, new ThrunTheLastTroll());
        harness.setGraveyard(player1, List.of(new NagaFleshcrafter()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, legendary.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
