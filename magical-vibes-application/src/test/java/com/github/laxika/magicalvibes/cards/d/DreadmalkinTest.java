package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dreadmalkin.class, GideonBlackblade.class, GrizzlyBears.class})
class DreadmalkinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts two +1/+1 counters on Dreadmalkin")
    void sacrificesAnotherCreatureAndGetsCounters() {
        Permanent dreadmalkin = addReadyDreadmalkin(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreadmalkin).doesNotContain(bears);
    }

    @Test
    @DisplayName("Sacrificing another planeswalker puts two +1/+1 counters on Dreadmalkin")
    void sacrificesAnotherPlaneswalkerAndGetsCounters() {
        Permanent dreadmalkin = addReadyDreadmalkin(player1);
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        gideon.setSummoningSick(false);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dreadmalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Gideon Blackblade");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dreadmalkin).doesNotContain(gideon);
    }

    @Test
    @DisplayName("The ability cannot sacrifice Dreadmalkin itself")
    void requiresAnotherPermanent() {
        addReadyDreadmalkin(player1);
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability requires two generic mana and one black mana")
    void requiresMana() {
        addReadyDreadmalkin(player1);
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDreadmalkin(Player player) {
        Permanent dreadmalkin = harness.addToBattlefieldAndReturn(player, new Dreadmalkin());
        dreadmalkin.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return dreadmalkin;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
