package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.c.CopyEnchantment;
import com.github.laxika.magicalvibes.cards.s.SabertoothWyvern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DominatingLicid.class, SabertoothWyvern.class, CityOfTraitors.class, CopyEnchantment.class})
class DominatingLicidTest extends BaseCardTest {

    @Test
    void canEndTheEffectAfterTheLicidLosesItsAbilities() {
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
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
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
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
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
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
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
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
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
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
        addCreatureReady(player1, new DominatingLicid());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new CityOfTraitors());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void copyingAuraFormCopiesThePrintedCreatureAndItsAbility() {
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        CopyEnchantment copyCard = new CopyEnchantment();
        harness.castFromHand(player1, copyCard, "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, licid.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getId().equals(copyCard.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, copy)).isTrue();
        assertThat(copy.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        copy.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(copy), null, host.getId());
        harness.passBothPriorities();

        assertThat(copy.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, copy)).isFalse();
    }

    @Test
    void hostLeavingPutsLicidIntoItsOwnersGraveyard() {
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(licid);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(licid.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(host.getOriginalCard());
    }

    @Test
    void sourceLeavingBeforeResolutionDoesNotStealTheTarget() {
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, licid));
        harness.passBothPriorities();

        assertThat(gd.findControllerOf(host)).isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(licid.getOriginalCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEndEffectWithoutBlueMana() {
        Permanent licid = addCreatureReady(player1, new DominatingLicid());
        Permanent host = addCreatureReady(player2, new SabertoothWyvern());
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gd.findControllerOf(host)).isEqualTo(player1.getId());
        assertThat(gd.stack).isEmpty();
    }
}
