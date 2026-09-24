package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Endling.class, LightningBolt.class})
class EndlingTest extends BaseCardTest {

    @Test
    void gainsMenaceDeathtouchAndUndyingUntilEndOfTurn() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, endling, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, endling, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, endling, Keyword.UNDYING)).isTrue();
    }

    @Test
    void undyingReturnsItWithACounterWhenItDies() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, endling.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returnedEndling = findPermanent(player1, "Endling");
        assertThat(returnedEndling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Endling");
    }

    @Test
    void firstPowerToughnessModeAppliesUntilEndOfTurn() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, 0, null);
        harness.passBothPriorities();

        assertThat(endling.getPowerModifier()).isEqualTo(1);
        assertThat(endling.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    void secondPowerToughnessModeAppliesUntilEndOfTurn() {
        Permanent endling = harness.addToBattlefieldAndReturn(player1, new Endling());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, 1, null);
        harness.passBothPriorities();

        assertThat(endling.getPowerModifier()).isEqualTo(-1);
        assertThat(endling.getToughnessModifier()).isEqualTo(1);
    }
}
