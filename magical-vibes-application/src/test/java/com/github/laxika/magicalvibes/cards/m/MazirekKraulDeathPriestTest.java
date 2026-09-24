package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({MazirekKraulDeathPriest.class, GrizzlyBears.class, KuldothaRebirth.class, Spellbook.class})
class MazirekKraulDeathPriestTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each creature you control when you sacrifice a permanent")
    void triggersForControllerSacrifice() {
        Permanent mazirek = addCreatureReady(player1, new MazirekKraulDeathPriest());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());

        castKuldothaRebirth(player1, artifact);
        harness.passBothPriorities();

        assertThat(mazirek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Triggers when an opponent sacrifices a permanent")
    void triggersForOpponentSacrifice() {
        Permanent mazirek = addCreatureReady(player1, new MazirekKraulDeathPriest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        castKuldothaRebirth(player2, artifact);
        harness.passBothPriorities();

        assertThat(mazirek.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Spellbook");
    }

    private void castKuldothaRebirth(com.github.laxika.magicalvibes.model.Player player,
                                     Permanent artifact) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new KuldothaRebirth()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player, 0, artifact.getId());
    }
}
