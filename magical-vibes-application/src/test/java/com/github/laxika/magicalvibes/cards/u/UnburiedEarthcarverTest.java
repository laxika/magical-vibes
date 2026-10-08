package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnburiedEarthcarver.class, GrizzlyBears.class})
class UnburiedEarthcarverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts a +1/+1 counter on Unburied Earthcarver")
    void sacrificingAnotherCreaturePutsCounterOnUnburiedEarthcarver() {
        Permanent earthcarver = addUnburiedEarthcarverReady(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(earthcarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Unburied Earthcarver cannot sacrifice itself")
    void cannotSacrificeItself() {
        addUnburiedEarthcarverReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Unburied Earthcarver");
    }

    @Test
    @DisplayName("Sacrifice is paid before the counter ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent earthcarver = harness.addToBattlefieldAndReturn(player1, new UnburiedEarthcarver());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UnburiedEarthcarver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(earthcarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(earthcarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent earthcarver = harness.addToBattlefieldAndReturn(player1, new UnburiedEarthcarver());
        earthcarver.tap();
        harness.addToBattlefield(player1, new UnburiedEarthcarver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(earthcarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(earthcarver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new UnburiedEarthcarver());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new UnburiedEarthcarver());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana prevents activation without sacrificing a creature")
    void insufficientManaDoesNotSacrificeCreature() {
        Permanent earthcarver = harness.addToBattlefieldAndReturn(player1, new UnburiedEarthcarver());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UnburiedEarthcarver());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(earthcarver, sacrifice);
        assertThat(gd.stack).isEmpty();
        assertThat(earthcarver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addUnburiedEarthcarverReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new UnburiedEarthcarver());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
