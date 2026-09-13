package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OverlaidTerrain;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaleMoon.class, Forest.class, YavimayaCoast.class, OverlaidTerrain.class, DryadArbor.class})
class PaleMoonTest extends BaseCardTest {

    private void resolvePaleMoon() {
        harness.castFromHand(player1, new PaleMoon(), "{1}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Nonbasic lands produce colorless mana instead of their chosen color")
    void nonbasicLandProducesColorlessMana() {
        resolvePaleMoon();
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Basic lands are unaffected")
    void basicLandIsUnaffected() {
        resolvePaleMoon();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The replacement applies to an opponent's nonbasic lands")
    void opponentNonbasicLandProducesColorlessMana() {
        resolvePaleMoon();
        harness.addToBattlefield(player2, new YavimayaCoast());

        harness.activateAbility(player2, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Preserves the amount produced by a nonbasic land")
    void preservesAmountProducedByNonbasicLand() {
        resolvePaleMoon();
        harness.addToBattlefield(player1, new OverlaidTerrain());
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.activateAbility(player1, 1, 3, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("A nonbasic land with a basic land type is affected")
    void nonbasicLandWithBasicLandTypeIsAffected() {
        resolvePaleMoon();
        addCreatureReady(player1, new DryadArbor());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The replacement wears off at end of turn")
    void replacementWearsOffAtEndOfTurn() {
        resolvePaleMoon();
        harness.addToBattlefield(player1, new YavimayaCoast());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        land.untap();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
