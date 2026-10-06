package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyclaveCleric.class, SkyclaveBasilica.class})
class SkyclaveClericTest extends BaseCardTest {

    @Test
    @DisplayName("Skyclave Cleric gains 2 life when it enters")
    void gainsLifeWhenItEnters() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SkyclaveCleric(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Skyclave Basilica enters tapped and produces white mana")
    void landFaceEntersTappedAndProducesWhiteMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SkyclaveCleric()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(SkyclaveBasilica.class);
        assertThat(land.isTapped()).isTrue();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain waits for the enter trigger to resolve")
    void lifeGainWaitsForEnterTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new SkyclaveCleric(), "{1}{W}");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Skyclave Cleric");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Entering without being cast gains life for the entering controller")
    void enteringWithoutCastingGainsLifeForController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);

        harness.enterBattlefieldAndReturn(player2, new SkyclaveCleric());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
    }
}
