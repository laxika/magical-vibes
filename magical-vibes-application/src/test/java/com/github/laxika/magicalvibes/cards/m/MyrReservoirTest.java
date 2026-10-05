package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.v.VulshokReplica;
import com.github.laxika.magicalvibes.cards.w.WingsOfVelisVel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrReservoir.class, CopperMyr.class, MyrGalvanizer.class,
        VulshokReplica.class, WingsOfVelisVel.class})
class MyrReservoirTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}{C} as Myr-only restricted mana")
    void tapForMyrOnlyMana() {
        addCreatureReady(player1, new MyrReservoir());

        harness.activateAbility(player1, 0, 0, null, null);

        // Mana ability resolves immediately
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Myr-only mana can pay for Myr creature spells")
    void myrManaCanCastMyrSpells() {
        addCreatureReady(player1, new MyrReservoir());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Tap for {C}{C}
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);

        // Copper Myr costs {2} — castable with Myr-only mana
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(0);
    }

    @Test
    @DisplayName("Myr-only mana cannot pay for non-Myr artifact spells")
    void myrManaCannotCastNonMyrArtifact() {
        addCreatureReady(player1, new MyrReservoir());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Tap for {C}{C}
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);

        // Vulshok Replica costs {3} — it's an artifact but NOT a Myr, so Myr-only mana can't pay for it
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new VulshokReplica()));
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("{3}, {T}: Return target Myr card from graveyard to hand")
    void returnTargetMyrFromGraveyard() {
        addCreatureReady(player1, new MyrReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card copperMyr = new CopperMyr();
        harness.setGraveyard(player1, List.of(copperMyr));

        // Activate ability 1 (index 1), targeting the Myr in graveyard
        harness.activateAbility(player1, 0, 1, null, copperMyr.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Copper Myr");
        harness.assertNotInGraveyard(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Cannot target non-Myr card with graveyard return ability")
    void cannotReturnNonMyrFromGraveyard() {
        addCreatureReady(player1, new MyrReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card nonMyr = new VulshokReplica();
        harness.setGraveyard(player1, List.of(nonMyr));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, nonMyr.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Myr-only mana can also pay for Myr activated ability costs")
    void myrManaCanPayForMyrAbilities() {
        // MyrGalvanizer has {1}, {T}: Untap each other Myr you control
        addCreatureReady(player1, new MyrReservoir());
        addCreatureReady(player1, new MyrGalvanizer());

        // Tap Reservoir for {C}{C}
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);

        // Activate Myr Galvanizer's ability ({1}, {T}) — Myr subtype, so Myr-only mana works
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        // 2 Myr-only mana - 1 used = 1 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(1);
    }

    @Test
    @DisplayName("Graveyard return ability requires tap so can't be used same turn as mana ability")
    void bothAbilitiesRequireTap() {
        addCreatureReady(player1, new MyrReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card copperMyr = new CopperMyr();
        harness.setGraveyard(player1, List.of(copperMyr));

        // Use mana ability first (taps the reservoir)
        harness.activateAbility(player1, 0, 0, null, null);

        // Now try to use the graveyard return ability — should fail because it's tapped
        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, copperMyr.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Myr-only mana pools clear at phase transition")
    void myrManaPoolClearsNormally() {
        addCreatureReady(player1, new MyrReservoir());

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);

        // Clear mana pool (simulates end of phase)
        gd.playerManaPools.get(player1.getId()).clear();
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(0);
    }

    @Test
    void cannotReturnMyrFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new MyrReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card myr = new CopperMyr();
        harness.setGraveyard(player2, List.of(myr));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, myr.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Copper Myr");
    }

    @Test
    void returnDoesNothingWhenTargetLeavesGraveyard() {
        harness.addToBattlefield(player1, new MyrReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card myr = new CopperMyr();
        harness.setGraveyard(player1, List.of(myr));
        harness.activateAbility(player1, 0, 1, null, myr.getId(), Zone.GRAVEYARD);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(myr));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Copper Myr");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reservoirManaCannotPayForAnotherReservoirsReturnAbility() {
        harness.addToBattlefield(player1, new MyrReservoir());
        harness.addToBattlefield(player1, new MyrReservoir());
        Card myr = new CopperMyr();
        harness.setGraveyard(player1, List.of(myr));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, myr.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Copper Myr");
    }

    @Test
    void restrictedManaEmptiesAtStepTransition() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MyrReservoir());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isZero();
    }

    @Test
    void restrictedManaCanPayForAbilityOfCreatureThatGainedMyrType() {
        harness.addToBattlefield(player1, new MyrReservoir());
        var replica = harness.addToBattlefieldAndReturn(player1, new VulshokReplica());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, replica.getId());
        assertThat(gqs.hasEffectiveSubtype(gd, replica, CardSubtype.MYR)).isTrue();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Vulshok Replica");
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(1);
    }

    @Test
    void returnAbilityRequiresThreeMana() {
        harness.addToBattlefield(player1, new MyrReservoir());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card myr = new CopperMyr();
        harness.setGraveyard(player1, List.of(myr));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, myr.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Copper Myr");
        assertThat(findPermanent(player1, "Myr Reservoir").isTapped()).isFalse();
    }
}
