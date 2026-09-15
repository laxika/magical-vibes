package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZebraUnicorn;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmielTheBlessed.class, GrizzlyBears.class, ZebraUnicorn.class})
class EmielTheBlessedTest extends BaseCardTest {

    @Test
    @DisplayName("Pays hybrid mana to put one counter on a non-Unicorn entering creature")
    void paysToPutOneCounterOnEnteringCreature() {
        harness.addToBattlefield(player1, new EmielTheBlessed());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteringCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays hybrid mana to put two counters on an entering Unicorn")
    void paysToPutTwoCountersOnEnteringUnicorn() {
        harness.addToBattlefield(player1, new EmielTheBlessed());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent enteringCreature = harness.enterBattlefieldAndReturn(player1, new ZebraUnicorn());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles and immediately returns another creature under its owner's control")
    void flickersAnotherCreature() {
        Permanent emiel = harness.addToBattlefieldAndReturn(player1, new EmielTheBlessed());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        int emielIndex = gd.playerBattlefields.get(player1.getId()).indexOf(emiel);
        harness.activateAbility(player1, emielIndex, 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Cannot target Emiel itself")
    void cannotTargetItself() {
        Permanent emiel = harness.addToBattlefieldAndReturn(player1, new EmielTheBlessed());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int emielIndex = gd.playerBattlefields.get(player1.getId()).indexOf(emiel);
        assertThatThrownBy(() -> harness.activateAbility(player1, emielIndex, 0, null, emiel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
