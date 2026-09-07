package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RottenmouthViper.class, Spellbook.class})
class RottenmouthViperTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a nonland permanent reduces the cost and the ETB trigger uses one blight counter")
    void sacrificesNonlandPermanentAndResolvesEtbTrigger() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of(spellbook.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent viper = findPermanent(player1, "Rottenmouth Viper");
        assertThat(viper.getCounterCount(CounterType.BLIGHT)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("The attack trigger counts all blight counters on Rottenmouth Viper")
    void attackTriggerCountsExistingBlightCounters() {
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RottenmouthViper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithSacrificeForReduction(player1, 0, null, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent viper = findPermanent(player1, "Rottenmouth Viper");
        viper.setCounterCount(CounterType.BLIGHT, 1);
        viper.setSummoningSick(false);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(viper)));
        harness.passBothPriorities();

        assertThat(viper.getCounterCount(CounterType.BLIGHT)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(2);
    }
}
