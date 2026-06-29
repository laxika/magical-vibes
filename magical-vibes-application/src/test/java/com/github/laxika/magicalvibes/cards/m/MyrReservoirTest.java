package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MyrReservoirTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}{C} as Myr-only restricted mana")
    void tapForMyrOnlyMana() {
        harness.addToBattlefield(player1, new MyrReservoir());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        harness.activateAbility(player1, 0, 0, null, null);

        // Mana ability resolves immediately
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);
    }

    @Test
    @DisplayName("Myr-only mana can pay for Myr creature spells")
    void myrManaCanCastMyrSpells() {
        harness.addToBattlefield(player1, new MyrReservoir());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
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
        harness.addToBattlefield(player1, new MyrReservoir());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Tap for {C}{C}
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);

        // Mind Stone costs {2} — it's an artifact but NOT a Myr, so Myr-only mana can't pay for it
        harness.setHand(player1, List.of(new MindStone()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("{3}, {T}: Return target Myr card from graveyard to hand")
    void returnTargetMyrFromGraveyard() {
        harness.addToBattlefield(player1, new MyrReservoir());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
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
        harness.addToBattlefield(player1, new MyrReservoir());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card elves = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elves));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, elves.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Myr-only mana can also pay for Myr activated ability costs")
    void myrManaCanPayForMyrAbilities() {
        // MyrGalvanizer has {1}, {T}: Untap each other Myr you control
        harness.addToBattlefield(player1, new MyrReservoir());
        harness.addToBattlefield(player1, new MyrGalvanizer());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

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
        harness.addToBattlefield(player1, new MyrReservoir());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));
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
        harness.addToBattlefield(player1, new MyrReservoir());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(2);

        // Clear mana pool (simulates end of phase)
        gd.playerManaPools.get(player1.getId()).clear();
        assertThat(gd.playerManaPools.get(player1.getId()).getMyrOnlyColorless()).isEqualTo(0);
    }
}
