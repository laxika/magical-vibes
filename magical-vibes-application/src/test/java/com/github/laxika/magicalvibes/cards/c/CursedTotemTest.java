package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AzimaetDrake;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({CursedTotem.class, AzimaetDrake.class, WallOfRoots.class, Forest.class})
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

    // ===== Helpers =====

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
