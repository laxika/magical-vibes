package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StingingLicid.class, Forest.class, FightingDrake.class})
class StingingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature and stops it being a creature")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new FightingDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("Tapping the enchanted creature deals 2 damage to that creature's controller")
    void tappingEnchantedCreatureDamagesItsController() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new FightingDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, java.util.List.of(indexOf(player2, host)));
        resolveAllTriggers();

        // Damage goes to the tapped creature's controller, not the Licid's controller.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and stops the tap trigger")
    void endCostRevertsLicidAndStopsTrigger() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new FightingDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();

        harness.setLife(player2, 20);
        declareAttackers(player2, java.util.List.of(indexOf(player2, host)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Ability fizzles and the Licid stays a creature if the target leaves")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new FightingDrake());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        gd.playerBattlefields.get(player2.getId()).remove(host);
        resolveAllTriggers();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new StingingLicid());
    }
}
