package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.k.KodamasMight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PetalmaneBakuTest extends BaseCardTest {

    @Test
    @DisplayName("May put a ki counter on itself when an Arcane spell is cast")
    void arcaneSpellAddsKiCounterWhenAccepted() {
        Permanent baku = addReadyBaku();
        harness.setHand(player1, List.of(new KodamasMight()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, baku.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes the chosen number of ki counters and adds that much mana of the chosen color")
    void removesXCountersForAnyColorMana() {
        Permanent baku = addReadyBaku();
        baku.setCounterCount(CounterType.KI, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class).maxValue()).isEqualTo(3);

        harness.handleXValueChosen(player1, 2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(baku.getCounterCount(CounterType.KI)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addReadyBaku() {
        Permanent baku = new Permanent(new PetalmaneBaku());
        baku.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(baku);
        return baku;
    }
}
