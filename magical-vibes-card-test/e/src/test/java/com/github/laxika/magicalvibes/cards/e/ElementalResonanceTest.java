package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AquastrandSpider;
import com.github.laxika.magicalvibes.cards.r.ReaperKing;
import com.github.laxika.magicalvibes.cards.s.SimicGuildmage;
import com.github.laxika.magicalvibes.cards.s.SimicGrowthChamber;
import com.github.laxika.magicalvibes.cards.w.Willbender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalResonance.class, AquastrandSpider.class, SimicGuildmage.class,
        SimicGrowthChamber.class, Willbender.class, ReaperKing.class})
class ElementalResonanceTest extends BaseCardTest {

    @Test
    void addsColorlessForGenericAndColoredManaCostSymbols() {
        Permanent enchanted = harness.enterBattlefieldAndReturn(player1, new AquastrandSpider());
        attachAura(enchanted);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void choosesEachHybridManaCostSymbolIndividually() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new SimicGuildmage());
        attachAura(enchanted);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void triggersOnlyDuringTheControllersFirstMainPhase() {
        Permanent enchanted = harness.enterBattlefieldAndReturn(player1, new AquastrandSpider());
        attachAura(enchanted);

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void addsManaForAnOpponentControlledEnchantedPermanent() {
        Permanent enchanted = harness.enterBattlefieldAndReturn(player2, new AquastrandSpider());
        attachAura(enchanted);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void waitsOnTheStackBeforeAddingMana() {
        Permanent enchanted = harness.enterBattlefieldAndReturn(player1, new AquastrandSpider());
        attachAura(enchanted);

        advanceToPrecombatMain(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void canEnchantALandAndAddsNoManaForItsAbsentManaCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SimicGrowthChamber());
        harness.setHand(player1, List.of(new ElementalResonance()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Elemental Resonance").getAttachedTo()).isEqualTo(land.getId());
        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void usesTheNewEnchantedPermanentWhenAuraMovesBeforeResolution() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new SimicGuildmage());
        Permanent spider = harness.enterBattlefieldAndReturn(player1, new AquastrandSpider());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SimicGrowthChamber());
        Permanent aura = attachAura(spider);

        advanceToPrecombatMain(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guildmage),
                1, null, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotTriggerDuringPostcombatMainPhase() {
        Permanent enchanted = harness.enterBattlefieldAndReturn(player1, new AquastrandSpider());
        attachAura(enchanted);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void addsNoManaForAFaceDownPermanent() {
        harness.setHand(player1, List.of(new Willbender()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        attachAura(findPermanent(player1, "Willbender"));

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canChooseTwoColorlessManaForEachMonocoloredHybridSymbol() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        attachAura(enchanted);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        for (int symbol = 0; symbol < 5; symbol++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
            harness.handleListChoice(player1, ManaColor.COLORLESS.name());
        }

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(10);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(10);
    }

    private Permanent attachAura(Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ElementalResonance());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
