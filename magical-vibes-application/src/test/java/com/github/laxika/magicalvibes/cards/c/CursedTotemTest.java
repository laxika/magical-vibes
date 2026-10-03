package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.t.TitaniasSong;
import com.github.laxika.magicalvibes.cards.w.WallOfRoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CursedTotem.class, AzimaetDrake.class, WallOfRoots.class, Forest.class,
        LlanowarElves.class, RodOfRuin.class, TitaniasSong.class})
class CursedTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Blocks non-mana activated abilities of creatures")
    void blocksCreatureActivatedAbilities() {
        addCursedTotem(player1);

        addCreatureWithActivatedAbility(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Cursed Totem");
    }

    @Test
    @DisplayName("Blocks activated abilities of own creatures")
    void blocksOwnCreatureActivatedAbilities() {
        addCursedTotem(player1);

        addCreatureWithActivatedAbility(player1);

        // Creature is at index 1 (after Cursed Totem at index 0)
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Cursed Totem");
    }

    @Test
    @DisplayName("Blocks mana abilities of creatures")
    void blocksCreatureManaAbilities() {
        addCursedTotem(player1);

        addCreatureManaAbility(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Cursed Totem");
    }

    @Test
    @DisplayName("Does NOT block mana abilities of non-creature permanents (lands)")
    void doesNotBlockLandManaAbilities() {
        addCursedTotem(player1);

        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing Cursed Totem re-enables creature abilities")
    void removingCursedTotemReenablesAbilities() {
        Permanent totem = addCursedTotem(player1);
        addCreatureWithActivatedAbility(player2);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        gd.playerBattlefields.get(player1.getId()).remove(totem);

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Blocks creatures using the tap-for-mana activation path without paying costs")
    void blocksTapForCreatureMana() {
        addCursedTotem(player1);
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cursed Totem");

        assertThat(elves.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not block non-mana activated abilities of noncreature artifacts")
    void allowsNoncreatureArtifactAbility() {
        addCursedTotem(player1);
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    @DisplayName("Does not stop a creature ability already on the stack")
    void allowsAlreadyActivatedAbilityToResolve() {
        Permanent drake = addCreatureReady(player1, new AzimaetDrake());
        harness.addMana(player1, ManaColor.BLUE, 1);
        int powerBefore = gqs.getEffectivePower(gd, drake);
        harness.activateAbility(player1, 0, 0, null, null);

        addCursedTotem(player2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(powerBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing all abilities to Titania's Song removes the creature activation restriction")
    void totemWithoutAbilitiesDoesNotBlockCreatureAbility() {
        addCursedTotem(player1);
        harness.addToBattlefield(player1, new TitaniasSong());
        Permanent drake = addCreatureReady(player2, new AzimaetDrake());
        harness.addMana(player2, ManaColor.BLUE, 1);
        int powerBefore = gqs.getEffectivePower(gd, drake);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drake)).isEqualTo(powerBefore + 1);
    }

    @Test
    @DisplayName("Losing all abilities to Titania's Song also removes the creature mana restriction")
    void totemWithoutAbilitiesDoesNotBlockCreatureMana() {
        addCursedTotem(player1);
        harness.addToBattlefield(player1, new TitaniasSong());
        addCreatureReady(player2, new LlanowarElves());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    private Permanent addCursedTotem(Player player) {
        return harness.addToBattlefieldAndReturn(player, new CursedTotem());
    }

    private void addCreatureWithActivatedAbility(Player player) {
        addCreatureReady(player, new AzimaetDrake());
    }

    private void addCreatureManaAbility(Player player) {
        addCreatureReady(player, new WallOfRoots());
    }
}
