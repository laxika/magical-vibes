package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThreeBowlsOfPorridge.class, GrizzlyBears.class})
class ThreeBowlsOfPorridgeTest extends BaseCardTest {

    @Test
    void damageModeDealsTwoDamageToTargetCreature() {
        harness.addToBattlefield(player1, new ThreeBowlsOfPorridge());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void tapModeTapsTargetCreature() {
        harness.addToBattlefield(player1, new ThreeBowlsOfPorridge());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void sacrificeModeSacrificesArtifactAndGainsLife() {
        harness.addToBattlefield(player1, new ThreeBowlsOfPorridge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Three Bowls of Porridge");
    }

    @Test
    void eachModeCanBeUsedOnce() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ThreeBowlsOfPorridge());
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tapTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, damageTarget.getId());
        harness.passBothPriorities();
        source.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, tapTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.activateAbility(player1, 0, 1, null, tapTarget.getId());
        harness.passBothPriorities();
        source.untap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Three Bowls of Porridge");
    }
}
