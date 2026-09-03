package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Censor;
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

@CardUsed({WitheringHex.class, AirElemental.class, Censor.class})
class WitheringHexTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card puts a plague counter on Withering Hex and weakens its enchanted creature")
    void cyclingAddsPlagueCounterAndWeakensEnchantedCreature() {
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new WitheringHex(), new Censor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, elemental.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Withering Hex");
        harness.setLibrary(player1, List.of(new AirElemental()));
        harness.activateHandAbility(player1, 0, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(aura.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cycling by an opponent also puts a plague counter on Withering Hex")
    void opponentCyclingAddsPlagueCounter() {
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        Permanent aura = new Permanent(new WitheringHex());
        aura.setAttachedTo(elemental.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new AirElemental()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateHandAbility(player2, 0, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(aura.getCounterCount(CounterType.PLAGUE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(3);
    }
}
