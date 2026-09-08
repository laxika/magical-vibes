package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({Spiritmonger.class, GrizzlyBears.class, PreyUpon.class})
class SpiritmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself after dealing damage to a creature")
    void putsCounterAfterDealingDamageToCreature() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(monger.getId(), target.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(monger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The black ability grants a regeneration shield")
    void regenerateAbilityGrantsShield() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The green ability changes its color until end of turn")
    void becomesChosenColorUntilEndOfTurn() {
        Permanent monger = addCreatureReady(player1, new Spiritmonger());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.getEffectiveColors(gd, monger)).containsExactly(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, monger)).containsExactly(CardColor.BLACK, CardColor.GREEN);
    }
}
