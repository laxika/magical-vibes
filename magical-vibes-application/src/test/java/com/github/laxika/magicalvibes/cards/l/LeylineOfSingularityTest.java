package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.o.OrzhovBasilica;
import com.github.laxika.magicalvibes.cards.s.SilhanaLedgewalker;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfSingularity.class, SilhanaLedgewalker.class, OrzhovBasilica.class})
class LeylineOfSingularityTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in the opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        LeylineOfSingularity leyline = new LeylineOfSingularity();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(leyline));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        openingHarness.assertOnBattlefield(openingHarness.getPlayer1(), "Leyline of Singularity");
        openingHarness.assertNotInHand(openingHarness.getPlayer1(), "Leyline of Singularity");
    }

    @Test
    @DisplayName("Declining the opening-hand ability keeps Leyline in hand")
    void decliningOpeningHandAbilityKeepsLeylineInHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        LeylineOfSingularity leyline = new LeylineOfSingularity();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(leyline));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        openingHarness.assertNotOnBattlefield(openingHarness.getPlayer1(), "Leyline of Singularity");
        openingHarness.assertInHand(openingHarness.getPlayer1(), "Leyline of Singularity");
    }

    @Test
    @DisplayName("All nonland permanents become legendary")
    void allNonlandPermanentsBecomeLegendary() {
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfSingularity());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SilhanaLedgewalker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new OrzhovBasilica());

        assertThat(gqs.hasEffectiveSupertype(gd, leyline, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, ownCreature, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, opposingCreature, CardSupertype.LEGENDARY)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isFalse();
    }

    @Test
    @DisplayName("The legend rule sees nonland permanents made legendary by Leyline of Singularity")
    void legendRuleSeesGrantedLegendarySupertype() {
        harness.addToBattlefield(player1, new LeylineOfSingularity());
        harness.addToBattlefield(player1, new SilhanaLedgewalker());
        harness.addToBattlefield(player1, new SilhanaLedgewalker());

        harness.runStateBasedActions();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.LegendRule.class);
    }

    @Test
    @DisplayName("Nonland permanents stop being legendary when Leyline of Singularity leaves")
    void effectEndsWhenLeylineLeaves() {
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfSingularity());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SilhanaLedgewalker());

        gd.playerBattlefields.get(player1.getId()).remove(leyline);

        assertThat(gqs.hasEffectiveSupertype(gd, creature, CardSupertype.LEGENDARY)).isFalse();
    }
}
