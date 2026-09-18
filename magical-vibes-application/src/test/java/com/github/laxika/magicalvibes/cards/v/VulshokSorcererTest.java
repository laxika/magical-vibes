package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GoblinBrawler;
import com.github.laxika.magicalvibes.cards.s.SylvokExplorer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VulshokSorcerer.class, SylvokExplorer.class, GoblinBrawler.class})
class VulshokSorcererTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows activating the tap ability the turn it enters")
    void hasteAllowsActivatingTheTurnItEnters() {
        harness.setHand(player1, List.of(new VulshokSorcerer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageDestroying1Toughness() {
        Permanent sorcerer = addCreatureReady(player1, new VulshokSorcerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SylvokExplorer());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sylvok Explorer");
        harness.assertInGraveyard(player2, "Sylvok Explorer");
        assertThat(sorcerer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 1 damage to target creature, 2/2 creature survives")
    void deals1DamageDoesNotKill2Toughness() {
        addCreatureReady(player1, new VulshokSorcerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinBrawler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Goblin Brawler");
    }

    @Test
    @DisplayName("Can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new VulshokSorcerer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SylvokExplorer());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sylvok Explorer");
        harness.assertInGraveyard(player1, "Sylvok Explorer");
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new VulshokSorcerer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoblinBrawler());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent sorcerer = addCreatureReady(player1, new VulshokSorcerer());
        sorcerer.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
