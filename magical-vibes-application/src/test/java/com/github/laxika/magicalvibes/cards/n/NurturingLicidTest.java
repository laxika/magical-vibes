package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NurturingLicid.class, LowlandGiant.class, Forest.class})
class NurturingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Licid ability attaches it to the target creature as an Aura")
    void abilityTurnsLicidIntoAttachedAura() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new LowlandGiant());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
    }

    @Test
    @DisplayName("Licid ability can attach to an opponent's creature")
    void attachesToOpponentCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new LowlandGiant());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
    }

    @Test
    @DisplayName("Licid ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent licid = addReadyLicid(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(licid.isTapped()).isFalse();
    }

    @Test
    @DisplayName("While attached, the regenerate ability shields the enchanted creature")
    void regeneratesEnchantedCreature() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new LowlandGiant());
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
        Permanent host = addCreatureReady(player1, new LowlandGiant());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, host.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid ability index");
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
        Permanent host = addCreatureReady(player1, new LowlandGiant());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    @Test
    @DisplayName("Targeting the Licid itself puts the resulting self-enchanting Aura into its owner's graveyard")
    void selfTargetIsPutIntoGraveyard() {
        Permanent licid = addReadyLicid(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, licid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nurturing Licid");
        harness.assertInGraveyard(player1, "Nurturing Licid");
    }

    @Test
    @DisplayName("Paying the end cost reverts the Aura immediately")
    void endCostIsImmediate() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new LowlandGiant());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new NurturingLicid());
    }
}
