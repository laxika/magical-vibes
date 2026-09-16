package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WitheringHex.class, BarkhideMauler.class})
class WitheringHexTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card puts a plague counter on Withering Hex and weakens its enchanted creature")
    void cyclingAddsPlagueCounterAndWeakensEnchantedCreature() {
        Permanent mauler = addCreatureReady(player1, new BarkhideMauler());
        harness.setHand(player1, List.of(new WitheringHex(), new BarkhideMauler()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, mauler.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Withering Hex");
        assertThat(aura.getCounterCount(CounterType.PLAGUE)).isZero();
        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(4);

        harness.setLibrary(player1, List.of(new BarkhideMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cycling by an opponent also puts a plague counter on Withering Hex")
    void opponentCyclingAddsPlagueCounter() {
        Permanent mauler = addCreatureReady(player1, new BarkhideMauler());
        Permanent aura = new Permanent(new WitheringHex());
        aura.setAttachedTo(mauler.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setHand(player2, List.of(new BarkhideMauler()));
        harness.setLibrary(player2, List.of(new BarkhideMauler()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each cycling event adds another plague counter and another -1/-1")
    void multipleCyclesScaleTheDebuff() {
        Permanent mauler = addCreatureReady(player1, new BarkhideMauler());
        Permanent aura = new Permanent(new WitheringHex());
        aura.setAttachedTo(mauler.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setHand(player1, List.of(new BarkhideMauler(), new BarkhideMauler()));
        harness.setLibrary(player1, List.of(new BarkhideMauler(), new BarkhideMauler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.PLAGUE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, mauler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mauler)).isEqualTo(2);
    }
}
