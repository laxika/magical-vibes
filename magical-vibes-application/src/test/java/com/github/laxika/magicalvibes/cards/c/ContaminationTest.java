package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.PriestOfTitania;
import com.github.laxika.magicalvibes.cards.t.TolarianAcademy;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Contamination.class, ArgothianSwine.class, Forest.class, Island.class, Mountain.class,
        PriestOfTitania.class, TolarianAcademy.class, WornPowerstone.class})
class ContaminationTest extends BaseCardTest {

    @Test
    @DisplayName("Lands produce black instead of their normal colors")
    void landsProduceBlack() {
        harness.addToBattlefield(player1, new Contamination());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.tapPermanent(player1, 1);
        harness.tapPermanent(player1, 2);
        harness.tapPermanent(player1, 3);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Contamination affects an opponent's lands")
    void affectsOpponentLands() {
        harness.addToBattlefield(player1, new Contamination());
        harness.addToBattlefield(player2, new Forest());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not replace mana from nonland permanents")
    void doesNotReplaceNonlandMana() {
        harness.addToBattlefield(player1, new Contamination());
        Permanent priest = harness.addToBattlefieldAndReturn(player1, new PriestOfTitania());
        priest.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Preserves the amount of mana produced by a land")
    void preservesMultipleManaProducedByLand() {
        harness.addToBattlefield(player1, new Contamination());
        harness.addToBattlefield(player1, new TolarianAcademy());
        harness.addToBattlefield(player1, new WornPowerstone());
        harness.addToBattlefield(player1, new WornPowerstone());

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Upkeep without a creature sacrifices Contamination")
    void upkeepWithoutCreatureSacrificesContamination() {
        harness.addToBattlefield(player1, new Contamination());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Contamination");
        harness.assertInGraveyard(player1, "Contamination");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Upkeep can sacrifice a creature to keep Contamination")
    void upkeepSacrificesCreatureToKeepContamination() {
        harness.addToBattlefield(player1, new Contamination());
        harness.addToBattlefield(player1, new ArgothianSwine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Argothian Swine").getId());

        harness.assertOnBattlefield(player1, "Contamination");
        harness.assertNotOnBattlefield(player1, "Argothian Swine");
        harness.assertInGraveyard(player1, "Argothian Swine");
    }

    @Test
    @DisplayName("Declining to sacrifice a creature sacrifices Contamination")
    void decliningToSacrificeCreatureSacrificesContamination() {
        harness.addToBattlefield(player1, new Contamination());
        harness.addToBattlefield(player1, new ArgothianSwine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Contamination");
        harness.assertInGraveyard(player1, "Contamination");
        harness.assertOnBattlefield(player1, "Argothian Swine");
    }

    @Test
    @DisplayName("An opponent's creature cannot pay Contamination's upkeep cost")
    void opponentCreatureDoesNotPayUpkeepCost() {
        harness.addToBattlefield(player1, new Contamination());
        harness.addToBattlefield(player2, new ArgothianSwine());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Contamination");
        harness.assertInGraveyard(player1, "Contamination");
        harness.assertOnBattlefield(player2, "Argothian Swine");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({DeepFreeze.class, Opalescence.class})
    @DisplayName("Its land-mana replacement is lost when Contamination loses all abilities")
    void losesStaticManaReplacementWhenItsAbilitiesAreRemoved() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent contamination = harness.addToBattlefieldAndReturn(player1, new Contamination());
        Permanent deepFreeze = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreeze.setAttachedTo(contamination.getId());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 3);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @CardUsed({DeepFreeze.class, Opalescence.class})
    @DisplayName("Its upkeep ability is lost when Contamination loses all abilities")
    void losesUpkeepAbilityWhenItsAbilitiesAreRemoved() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent contamination = harness.addToBattlefieldAndReturn(player1, new Contamination());
        Permanent deepFreeze = harness.addToBattlefieldAndReturn(player1, new DeepFreeze());
        deepFreeze.setAttachedTo(contamination.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Contamination");
    }
}
