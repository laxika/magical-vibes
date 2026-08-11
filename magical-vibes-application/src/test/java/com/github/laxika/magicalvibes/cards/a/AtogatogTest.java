package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AtogatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Atog gives Atogatog +X/+X based on its power")
    void sacrificeBoostsBySacrificedPower() {
        harness.addToBattlefield(player1, new Atogatog());
        harness.addToBattlefield(player1, new Atog());

        Permanent atogatog = findPermanent(player1, "Atogatog");
        Permanent atog = findPermanent(player1, "Atog");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Atog");
        assertThat(atogatog.getPowerModifier()).isEqualTo(1);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Uses the sacrificed Atog's effective power")
    void usesEffectivePower() {
        harness.addToBattlefield(player1, new Atogatog());
        Permanent atog = addCreatureReady(player1, new Atog());
        atog.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        Permanent atogatog = findPermanent(player1, "Atogatog");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.passBothPriorities();

        assertThat(atogatog.getPowerModifier()).isEqualTo(2);
        assertThat(atogatog.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice Atogatog itself")
    void canSacrificeItself() {
        harness.addToBattlefield(player1, new Atogatog());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Atogatog");
    }

    @Test
    @DisplayName("Only Atog creatures can be chosen for the sacrifice")
    void onlyAtogsCanBeChosen() {
        harness.addToBattlefield(player1, new Atogatog());
        Permanent atog = addCreatureReady(player1, new Atog());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, atog.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Atog");
    }
}
