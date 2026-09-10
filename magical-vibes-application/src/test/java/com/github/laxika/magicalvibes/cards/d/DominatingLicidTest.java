package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.s.SabertoothWyvern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DominatingLicid.class, SabertoothWyvern.class, CityOfTraitors.class})
class DominatingLicidTest extends BaseCardTest {

    @Test
    void canEndTheEffectAfterTheLicidLosesItsAbilities() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        licid.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability attaches the Licid to a creature and takes control of it")
    void abilityTurnsLicidIntoControlAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
        assertThat(gd.findControllerOf(host)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Paying the end cost reverts the Licid and returns control of the creature")
    void endCostRevertsLicidAndControl() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gd.findControllerOf(host)).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Paying the end cost reverts the Licid immediately")
    void payingEndCostIsImmediate() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gd.findControllerOf(host)).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("An illegal target on resolution leaves the Licid as a creature")
    void illegalTargetOnResolutionLeavesLicidAsCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyLicid(Player player) {
        Permanent perm = new Permanent(new DominatingLicid());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
