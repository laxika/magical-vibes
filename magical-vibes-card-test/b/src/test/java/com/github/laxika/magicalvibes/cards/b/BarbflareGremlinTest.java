package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.ReliquaryTower;
import com.github.laxika.magicalvibes.cards.s.SimicGrowthChamber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbflareGremlin.class, Forest.class, SimicGrowthChamber.class, ReliquaryTower.class})
class BarbflareGremlinTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Barbflare Gremlin doubles land mana and deals damage")
    void tappedGremlinTriggersForControllerLand() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An untapped Barbflare Gremlin does not trigger")
    void untappedGremlinDoesNotTrigger() {
        addCreatureReady(player1, new BarbflareGremlin());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A tapped Barbflare Gremlin triggers for an opponent's land")
    void tappedGremlinTriggersForOpponentLand() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        harness.addToBattlefield(player2, new Forest());
        harness.setLife(player2, 20);

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The tapped land is the damage source, rather than Barbflare Gremlin")
    void landDealsTheDamage() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(land.getId(), 0)).isEqualTo(1);
        assertThat(gd.damageDealtThisTurnBySource.getOrDefault(gremlin.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A land producing two mana types grants only one extra mana of the chosen type")
    void multicolorLandAddsOnlyOneChosenMana() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        harness.addToBattlefield(player2, new SimicGrowthChamber());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.handleListChoice(player2, "BLUE");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage follows the extra mana choice when the land produces multiple types")
    void damageWaitsForExtraManaChoice() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        harness.addToBattlefield(player2, new SimicGrowthChamber());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "GREEN");

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Each tapped Gremlin independently adds one mana and makes the land deal one damage")
    void multipleTappedGremlinsEachTrigger() {
        addCreatureReady(player1, new BarbflareGremlin()).tap();
        addCreatureReady(player2, new BarbflareGremlin()).tap();
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana is a valid type for the extra mana")
    void colorlessLandAddsExtraColorlessMana() {
        addCreatureReady(player1, new BarbflareGremlin()).tap();
        harness.addToBattlefield(player2, new ReliquaryTower());
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untapping the Gremlin stops its ability for subsequent land activations")
    void untappedGremlinStopsAddingManaAndDamage() {
        Permanent gremlin = addCreatureReady(player1, new BarbflareGremlin());
        gremlin.tap();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        harness.tapPermanent(player1, 1);
        gremlin.untap();
        harness.tapPermanent(player1, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        harness.assertLife(player1, 19);
    }
}
