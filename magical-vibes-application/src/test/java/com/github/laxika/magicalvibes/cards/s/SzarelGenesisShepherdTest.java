package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.Treasure;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SzarelGenesisShepherd.class, Clue.class, Treasure.class, GrizzlyBears.class, Forest.class})
class SzarelGenesisShepherdTest extends BaseCardTest {

    @Test
    void playsLandFromGraveyard() {
        harness.addToBattlefield(player1, new SzarelGenesisShepherd());
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.playLandFromGraveyard(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void sacrificingAnotherNontokenPermanentDuringYourTurnUsesCurrentPower() {
        Permanent szarel = harness.addToBattlefieldAndReturn(player1, new SzarelGenesisShepherd());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Clue());
        szarel.setPowerModifier(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerForTokenSacrifice() {
        Permanent szarel = harness.addToBattlefieldAndReturn(player1, new SzarelGenesisShepherd());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Treasure treasure = new Treasure();
        treasure.setToken(true);
        harness.addToBattlefield(player1, treasure);
        szarel.setPowerModifier(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
