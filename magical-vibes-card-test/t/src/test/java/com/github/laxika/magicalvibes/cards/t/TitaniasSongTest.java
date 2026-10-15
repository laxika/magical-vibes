package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AmuletOfKroog;
import com.github.laxika.magicalvibes.cards.b.BottleOfSuleiman;
import com.github.laxika.magicalvibes.cards.d.DragonEngine;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Flight;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitaniasSong.class, AmuletOfKroog.class, BottleOfSuleiman.class,
        GrizzlyBears.class, DragonEngine.class, MarchOfTheMachines.class, Flight.class, MycosynthLattice.class})
class TitaniasSongTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature artifact becomes a creature with P/T equal to mana value")
    void animatesNoncreatureArtifact() {
        // Amulet of Kroog costs {2}, so mana value = 2
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new AmuletOfKroog());
        harness.addToBattlefield(player1, new TitaniasSong());

        assertThat(gqs.isCreature(gd, amulet)).isTrue();
        assertThat(gqs.getEffectivePower(gd, amulet)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, amulet)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not animate creatures")
    void doesNotAffectCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TitaniasSong());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not remove abilities from existing artifact creatures")
    void doesNotStripArtifactCreatureAbilities() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonEngine());
        harness.addToBattlefield(player1, new TitaniasSong());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int powerBefore = gqs.getEffectivePower(gd, dragon);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(powerBefore + 1);
    }

    @Test
    @DisplayName("Animated artifact loses its activated ability")
    void stripsArtifactAbilities() {
        // Bottle of Suleiman's ability has no tap symbol, so any activation failure is
        // due to losing all abilities rather than summoning sickness.
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addToBattlefield(player1, new TitaniasSong());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(MarchOfTheMachines.class)
    @DisplayName("March of the Machines animates the same artifact but keeps its ability")
    void marchAnimatesWithoutStrippingAbilities() {
        // Contrast: March of the Machines animates but does NOT remove abilities, so the
        // non-tap ability still works — isolating ability loss as Titania's Song behavior.
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        // Sacrifice is a cost, so the Bottle leaves the battlefield when the ability is used.
        harness.assertNotOnBattlefield(player1, "Bottle of Suleiman");
    }

    @Test
    @DisplayName("Animated artifacts stay creatures until end of turn after Song leaves")
    void effectContinuesUntilEndOfTurnAfterSongLeaves() {
        Permanent bottle = harness.addToBattlefieldAndReturn(player1, new BottleOfSuleiman());
        harness.addToBattlefield(player1, new TitaniasSong());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.isCreature(gd, bottle)).isTrue();

        Permanent song = findPermanent(player1, "Titania's Song");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, song));

        assertThat(gqs.isCreature(gd, bottle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bottle)).isEqualTo(4);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bottle)).isFalse();
    }

    @Test
    @DisplayName("Affects artifacts on both sides of the battlefield")
    void affectsBothPlayersArtifacts() {
        harness.addToBattlefield(player1, new TitaniasSong());
        Permanent opponentAmulet = harness.addToBattlefieldAndReturn(player2, new AmuletOfKroog());

        assertThat(gqs.isCreature(gd, opponentAmulet)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentAmulet)).isEqualTo(2);
    }

    @Test
    @CardUsed(MarchOfTheMachines.class)
    @DisplayName("Song does not strip abilities from artifacts already animated by an earlier March")
    void earlierMarchKeepsArtifactAbilities() {
        Permanent bottle = harness.addToBattlefieldAndReturn(player1, new BottleOfSuleiman());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addToBattlefield(player1, new TitaniasSong());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(gqs.isCreature(gd, bottle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bottle)).isEqualTo(4);
        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Bottle of Suleiman");
    }

    @Test
    @CardUsed(MarchOfTheMachines.class)
    @DisplayName("An earlier Song still strips abilities when March enters later")
    void earlierSongStillStripsAbilitiesWithLaterMarch() {
        harness.addToBattlefield(player1, new BottleOfSuleiman());
        harness.addToBattlefield(player1, new TitaniasSong());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(Flight.class)
    @DisplayName("Song's continuing effect preserves abilities granted after Song entered")
    void leavingDoesNotChangeAbilityRemovalTimestamp() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new AmuletOfKroog());
        Permanent song = harness.addToBattlefieldAndReturn(player1, new TitaniasSong());
        harness.setHand(player1, List.of(new Flight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, amulet.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, amulet, Keyword.FLYING)).isTrue();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, song));

        assertThat(gqs.isCreature(gd, amulet)).isTrue();
        assertThat(gqs.hasKeyword(gd, amulet, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The continuing effect also animates artifacts entering after Song leaves")
    void affectsArtifactsEnteringAfterSongLeaves() {
        Permanent song = harness.addToBattlefieldAndReturn(player1, new TitaniasSong());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, song));
        Permanent bottle = harness.addToBattlefieldAndReturn(player2, new BottleOfSuleiman());
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThat(gqs.isCreature(gd, bottle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bottle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bottle)).isEqualTo(4);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bottle)).isFalse();
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    @DisplayName("Song animates itself when an earlier Lattice makes it an artifact")
    void animatesItselfWithEarlierLattice() {
        Permanent lattice = harness.addToBattlefieldAndReturn(player1, new MycosynthLattice());
        Permanent song = harness.addToBattlefieldAndReturn(player1, new TitaniasSong());

        assertThat(gqs.isCreature(gd, song)).isTrue();
        assertThat(gqs.getEffectivePower(gd, song)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, song)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, lattice)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lattice)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, lattice)).isEqualTo(6);
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    @DisplayName("Song animates itself when Lattice enters after it")
    void animatesItselfWithLaterLattice() {
        Permanent song = harness.addToBattlefieldAndReturn(player1, new TitaniasSong());
        Permanent lattice = harness.addToBattlefieldAndReturn(player1, new MycosynthLattice());

        assertThat(gqs.isCreature(gd, song)).isTrue();
        assertThat(gqs.getEffectivePower(gd, song)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, song)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, lattice)).isTrue();
        assertThat(gqs.getEffectivePower(gd, lattice)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, lattice)).isEqualTo(6);
    }
}
