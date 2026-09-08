package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DraconauticsEngineer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Afterburner Expert")
class AfterburnerExpertTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts two +1/+1 counters on Afterburner Expert")
    void exhaustPutsTwoCountersOnIt() {
        Permanent expert = harness.addToBattlefieldAndReturn(player1, new AfterburnerExpert());
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(expert.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exhaust trigger returns Afterburner Expert before the ability resolves")
    void exhaustTriggerReturnsItFromGraveyard() {
        AfterburnerExpert expert = new AfterburnerExpert();
        harness.setGraveyard(player1, List.of(expert));
        harness.addToBattlefield(player1, new DraconauticsEngineer());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent returnedExpert = findPermanent(player1, "Afterburner Expert");
        harness.assertNotInGraveyard(player1, "Afterburner Expert");
        assertThat(gqs.hasKeyword(gd, returnedExpert, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, returnedExpert, Keyword.HASTE)).isTrue();
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
