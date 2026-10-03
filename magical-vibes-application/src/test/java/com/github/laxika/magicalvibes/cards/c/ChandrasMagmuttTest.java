package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChandrasMagmutt.class, ChandraHeartOfFire.class, AlpineWatchdog.class})
class ChandrasMagmuttTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void tapAbilityDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent magmutt = addCreatureReady(player1, new ChandrasMagmutt());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(magmutt.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to target planeswalker")
    void tapAbilityDealsDamageToPlaneswalker() {
        addCreatureReady(player1, new ChandrasMagmutt());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHeartOfFire());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tap ability cannot target a creature")
    void tapAbilityCannotTargetCreature() {
        addCreatureReady(player1, new ChandrasMagmutt());
        Permanent watchdog = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, watchdog.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tap ability can target its controller")
    void tapAbilityCanTargetController() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ChandrasMagmutt());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Tap ability cannot be activated while summoning sick")
    void tapAbilityCannotBeActivatedWhileSummoningSick() {
        harness.addToBattlefield(player1, new ChandrasMagmutt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Tap ability cannot be activated while already tapped")
    void tapAbilityCannotBeActivatedWhileTapped() {
        Permanent magmutt = addCreatureReady(player1, new ChandrasMagmutt());
        magmutt.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }

    @Test
    @DisplayName("Tap cost is paid before damage resolves")
    void tapCostIsPaidBeforeDamageResolves() {
        harness.setLife(player2, 20);
        Permanent magmutt = addCreatureReady(player1, new ChandrasMagmutt());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(magmutt.isTapped()).isTrue();
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Activated ability resolves after its source leaves the battlefield")
    void activatedAbilityResolvesAfterSourceLeaves() {
        harness.setLife(player2, 20);
        Permanent magmutt = addCreatureReady(player1, new ChandrasMagmutt());

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(magmutt);
        gd.playerGraveyards.get(player1.getId()).add(magmutt.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Tap ability does not damage a planeswalker that has left the battlefield")
    void tapAbilityDoesNotDamageDepartedPlaneswalker() {
        addCreatureReady(player1, new ChandrasMagmutt());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHeartOfFire());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
