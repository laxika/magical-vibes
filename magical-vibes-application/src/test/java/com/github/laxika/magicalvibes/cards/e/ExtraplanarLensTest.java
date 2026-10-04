package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArixmethesSlumberingIsle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmervoid;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArixmethesSlumberingIsle.class, ExtraplanarLens.class, Forest.class, Glimmervoid.class,
        Mountain.class, PullFromEternity.class, WordOfSeizing.class})
class ExtraplanarLensTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles the targeted land and imprints it")
    void acceptsImprint() {
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castFromHand(player1, new ExtraplanarLens(), "{3}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Forest");
        Permanent lens = findPermanent(player1, "Extraplanar Lens");
        assertThat(gd.getImprintedCard(lens.getCard()).getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the targeted land on the battlefield")
    void declinesImprint() {
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castFromHand(player1, new ExtraplanarLens(), "{3}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getImprintedCard(findPermanent(player1, "Extraplanar Lens").getCard()))
                .isNull();
    }

    @Test
    @DisplayName("The ETB ability is skipped when its controller controls no land")
    void skipsImprintWhenControllerHasNoLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new ExtraplanarLens(), "{3}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Extraplanar Lens");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getImprintedCard(findPermanent(player1, "Extraplanar Lens").getCard()))
                .isNull();
    }

    @Test
    @DisplayName("A matching land tapped by the controller adds one extra mana")
    void addsManaForMatchingControllerLand() {
        addLensWithImprintedForest();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("A matching land tapped by an opponent adds one extra mana to that opponent")
    void addsManaForMatchingOpponentLand() {
        addLensWithImprintedForest();
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("A matching any-color land adds the extra mana of the color it produced")
    void addsManaOfColorProducedByMatchingLand() {
        ExtraplanarLens lens = new ExtraplanarLens();
        harness.addToBattlefield(player1, lens);
        Glimmervoid imprintedLand = new Glimmervoid();
        harness.setExile(player1, List.of(imprintedLand));
        gd.setImprintedCard(lens, imprintedLand);
        harness.addToBattlefield(player1, new Glimmervoid());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A matching land producing multiple colors receives only one extra mana")
    void addsOnlyOneManaWhenMatchingLandProducesMultipleColors() {
        ExtraplanarLens lens = new ExtraplanarLens();
        harness.addToBattlefield(player1, lens);
        ArixmethesSlumberingIsle imprintedLand = new ArixmethesSlumberingIsle();
        harness.setExile(player1, List.of(imprintedLand));
        gd.setImprintedCard(lens, imprintedLand);

        Permanent arixmethes = addCreatureReady(player1, new ArixmethesSlumberingIsle());
        arixmethes.setCounterCount(CounterType.SLUMBER, 1);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("A land with a different name does not receive the mana bonus")
    void ignoresDifferentLandName() {
        addLensWithImprintedForest();
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana bonus does not apply before a land is imprinted")
    void ignoresLandWithoutImprint() {
        harness.addToBattlefield(player1, new ExtraplanarLens());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana bonus stops when the imprinted land leaves exile")
    void stopsAddingManaAfterImprintedLandLeavesExile() {
        Forest imprintedForest = new Forest();
        harness.addToBattlefield(player1, imprintedForest);
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castFromHand(player1, new ExtraplanarLens(), "{3}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forestId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, imprintedForest.getId());
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.findExiledCard(imprintedForest.getId())).isNull();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Changing control of the Lens before its ETB resolves preserves the imprint")
    void imprintsEvenIfLensChangesControllerBeforeResolution() {
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castFromHand(player1, new ExtraplanarLens(), "{3}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forestId);

        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Extraplanar Lens"));
        harness.assertOnBattlefield(player2, "Extraplanar Lens");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    private void addLensWithImprintedForest() {
        ExtraplanarLens lens = new ExtraplanarLens();
        harness.addToBattlefield(player1, lens);
        Forest imprintedLand = new Forest();
        harness.setExile(player1, List.of(imprintedLand));
        gd.setImprintedCard(lens, imprintedLand);
    }
}
