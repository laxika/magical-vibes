package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.InfernalDarkness;
import com.github.laxika.magicalvibes.cards.o.OverlaidTerrain;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaleMoon.class, Forest.class, YavimayaCoast.class, OverlaidTerrain.class, DryadArbor.class,
        InfernalDarkness.class})
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
        harness.passUntil(TurnStep.UPKEEP);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("A nonbasic land already on the battlefield is affected without losing its damage rider")
    void existingLandPreservesDamageRider() {
        harness.addToBattlefield(player1, new YavimayaCoast());
        int lifeBefore = gd.getLife(player1.getId());
        resolvePaleMoon();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Already colorless mana remains colorless")
    void colorlessManaIsUnchanged() {
        resolvePaleMoon();
        harness.addToBattlefield(player1, new YavimayaCoast());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("The land controller chooses the order of competing mana type replacements")
    void competingManaReplacementsRequireControllerChoice() {
        harness.addToBattlefield(player1, new InfernalDarkness());
        harness.addToBattlefield(player2, new YavimayaCoast());
        resolvePaleMoon();

        harness.activateAbility(player2, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
    }
}
