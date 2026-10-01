package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AquastrandSpider;
import com.github.laxika.magicalvibes.cards.s.SimicGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalResonance.class, AquastrandSpider.class, SimicGuildmage.class})
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
