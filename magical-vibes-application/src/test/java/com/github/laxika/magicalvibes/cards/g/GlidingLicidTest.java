package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlidingLicid.class, YouthfulKnight.class, VolrathsStronghold.class})
class GlidingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature as an Aura")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
        assertThat(licid.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attached Licid gives the enchanted creature flying")
    void attachedLicidGrantsFlying() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThat(gqs.hasKeyword(gd, host, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, host, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and turns it back into a creature")
    void endCostRevertsLicidToCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).as("ending a Licid effect is a special action").isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles and leaves the Licid a creature if its target leaves")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        gd.playerBattlefields.get(player2.getId()).remove(host);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new GlidingLicid());
    }
}
