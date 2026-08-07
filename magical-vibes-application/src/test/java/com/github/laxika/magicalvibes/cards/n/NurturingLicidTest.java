package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NurturingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Licid ability attaches it to the target creature as an Aura")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("While attached, the regenerate ability shields the enchanted creature")
    void regeneratesEnchantedCreature() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(host.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regenerate ability survives becoming an Aura but the Licid ability does not")
    void keepsRegenerateAbilityLosesLicidAbility() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getCard().getActivatedAbilities()).hasSize(2);
        assertThat(licid.getCard().getActivatedAbilities().getFirst().isRequiresTap()).isFalse();
    }

    @Test
    @DisplayName("Regenerating while still a creature does nothing — there is no enchanted creature")
    void regenerateDoesNothingWhileUnattached() {
        Permanent licid = addReadyLicid(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(licid.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and turns it back into a creature")
    void endCostRevertsLicidToCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    private Permanent addReadyLicid(Player player) {
        Permanent perm = new Permanent(new NurturingLicid());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
