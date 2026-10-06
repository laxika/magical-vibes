package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.i.IntangibleVirtue;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverchaseFox.class, IntangibleVirtue.class, AbbeyGriffin.class})
class SilverchaseFoxTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Silverchase Fox and puts ability on stack")
    void activatingSacrificesSelfAndPutsOnStack() {
        addReadyFox(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        // Fox is sacrificed as cost (goes to graveyard)
        harness.assertNotOnBattlefield(player1, "Silverchase Fox");
        harness.assertInGraveyard(player1, "Silverchase Fox");
        // Ability is on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving ability exiles target enchantment")
    void resolvingExilesTargetEnchantment() {
        addReadyFox(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Intangible Virtue");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Intangible Virtue"));
        harness.assertNotInGraveyard(player2, "Intangible Virtue");
    }


    @Test
    @DisplayName("Cannot target a non-enchantment permanent")
    void cannotTargetNonEnchantment() {
        addReadyFox(player1);
        Permanent creature = addCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyFox(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Consumes {1}{W} mana when activating")
    void consumesMana() {
        addReadyFox(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }


    @Test
    @DisplayName("Ability fizzles if target enchantment is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyFox(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        // Fox is still in graveyard (cost was already paid)
        harness.assertInGraveyard(player1, "Silverchase Fox");
    }

    @Test
    @DisplayName("Can activate while summoning sick and tapped")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new SilverchaseFox());
        fox.setSummoningSick(true);
        fox.setTapped(true);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Intangible Virtue");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Intangible Virtue"));
    }

    @Test
    @DisplayName("Can exile an enchantment controlled by the ability's controller")
    void canExileOwnEnchantment() {
        addReadyFox(player1);
        Permanent target = addEnchantment(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Silverchase Fox");
        harness.assertNotOnBattlefield(player1, "Intangible Virtue");
        harness.assertNotInGraveyard(player1, "Intangible Virtue");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Intangible Virtue"));
    }

    @Test
    @DisplayName("Cannot activate with enough total mana but no white mana")
    void cannotActivateWithoutWhiteMana() {
        addReadyFox(player1);
        Permanent target = addEnchantment(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Silverchase Fox");
        harness.assertNotInGraveyard(player1, "Silverchase Fox");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private Permanent addReadyFox(Player player) {
        return addCreatureReady(player, new SilverchaseFox());
    }

    private Permanent addEnchantment(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IntangibleVirtue());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new AbbeyGriffin());
    }
}
