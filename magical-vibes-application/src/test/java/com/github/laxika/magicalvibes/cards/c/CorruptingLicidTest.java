package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptingLicid.class, YouthfulKnight.class, VolrathsStronghold.class})
class CorruptingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature as an Aura")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("Attached Licid gives the enchanted creature fear")
    void attachedLicidGrantsFear() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThat(gqs.hasKeyword(gd, host, Keyword.FEAR)).isFalse();

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, host, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and turns it back into a creature")
    void endCostRevertsLicidToCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gqs.hasKeyword(gd, host, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Paying the end cost is a special action and does not use the stack")
    void endCostIsSpecialAction() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).as("ending a Licid effect is a special action").isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking the enchanted creature")
    void enchantedCreatureCannotBeBlockedByNormalCreature() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        host.setAttacking(true);
        addCreatureReady(player2, new YouthfulKnight());
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability fizzles and leaves the Licid a creature if its target leaves")
    void fizzlesIfTargetLeaves() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new YouthfulKnight());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        gd.playerBattlefields.get(player2.getId()).remove(host);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new VolrathsStronghold());
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new CorruptingLicid());
    }
}
