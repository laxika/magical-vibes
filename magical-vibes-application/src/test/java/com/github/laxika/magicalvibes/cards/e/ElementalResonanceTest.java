package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalResonance.class, GrizzlyBears.class, SafeholdElite.class})
class ElementalResonanceTest extends BaseCardTest {

    @Test
    void addsColorlessForGenericAndColoredManaCostSymbols() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachAura(enchanted);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void choosesEachHybridManaCostSymbolIndividually() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new SafeholdElite());
        attachAura(enchanted);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void triggersOnlyDuringTheControllersFirstMainPhase() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachAura(enchanted);

        advanceToPrecombatMain(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private Permanent attachAura(Permanent enchanted) {
        Permanent aura = new Permanent(new ElementalResonance());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
