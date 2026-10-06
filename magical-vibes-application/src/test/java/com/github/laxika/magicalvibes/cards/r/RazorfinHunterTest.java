package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GaeasSkyfolk;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorfinHunter.class, GaeasSkyfolk.class, UrborgElf.class, YavimayaCoast.class})
class RazorfinHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RazorfinHunter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        addCreatureReady(player1, new RazorfinHunter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new UrborgElf()).getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Urborg Elf");
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addCreatureReady(player1, new RazorfinHunter());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GaeasSkyfolk()).getId();
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gaea's Skyfolk");
    }

    @Test
    @DisplayName("Ability taps the creature and cannot be activated twice")
    void abilityTapsAndCannotRepeat() {
        Permanent hunter = addCreatureReady(player1, new RazorfinHunter());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(hunter.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new RazorfinHunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent hunter = addCreatureReady(player1, new RazorfinHunter());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
        assertThat(hunter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target itself and dies to its own damage")
    void canTargetItself() {
        Permanent hunter = addCreatureReady(player1, new RazorfinHunter());

        harness.activateAbility(player1, 0, null, hunter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Razorfin Hunter");
        harness.assertNotOnBattlefield(player1, "Razorfin Hunter");
    }

    @Test
    @DisplayName("Ability still resolves after its source is killed in response")
    void abilityResolvesAfterSourceDies() {
        harness.setLife(player2, 20);
        Permanent hunter = addCreatureReady(player1, new RazorfinHunter());
        addCreatureReady(player2, new RazorfinHunter());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.activateAbility(player2, 0, null, hunter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Razorfin Hunter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
