package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HekmaSentinelsTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card gives this creature +1/+1")
    void cyclingBoostsSelf() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        // Cycling is a discard (CR 702.29e), so cycling Censor triggers the +1/+1.
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // resolve the boost trigger

        Permanent hekma = getHekmaSentinels();
        assertThat(hekma.getPowerModifier()).isEqualTo(1);
        assertThat(hekma.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each discard stacks another +1/+1")
    void discardsStack() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        harness.setHand(player1, List.of(new Censor(), new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent hekma = getHekmaSentinels();
        assertThat(hekma.getPowerModifier()).isEqualTo(2);
        assertThat(hekma.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new HekmaSentinels());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        Permanent hekma = getHekmaSentinels();
        assertThat(hekma.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(hekma.getPowerModifier()).isEqualTo(0);
        assertThat(hekma.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent getHekmaSentinels() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hekma Sentinels"))
                .findFirst()
                .orElseThrow();
    }
}
