package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StripMine.class})
class StripMineTest extends BaseCardTest {

    @Test
    @DisplayName("Can tap for colorless mana with first ability")
    void canTapForColorlessMana() {
        harness.addToBattlefield(player1, new StripMine());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @CardUsed({Forest.class})
    @DisplayName("Activating destroy ability sacrifices Strip Mine and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        harness.addToBattlefield(player1, new StripMine());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Strip Mine");
        harness.assertInGraveyard(player1, "Strip Mine");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @CardUsed({Forest.class})
    @DisplayName("Resolving destroys the target land, including a basic land")
    void resolvingDestroysTargetLand() {
        harness.addToBattlefield(player1, new StripMine());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @CardUsed({Forest.class})
    @DisplayName("Can target and destroy a land you control")
    void canDestroyOwnLand() {
        harness.addToBattlefield(player1, new StripMine());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Forest()).getId();

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @CardUsed({GrizzlyBears.class})
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new StripMine());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({Forest.class})
    @DisplayName("Cannot activate destroy ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new StripMine());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player1.getId()).getFirst().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate destroy ability without a target")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new StripMine());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Strip Mine");
        harness.assertNotInGraveyard(player1, "Strip Mine");
    }

    @Test
    @CardUsed({Forest.class})
    @DisplayName("Targeting Strip Mine itself fizzles after its sacrifice cost is paid")
    void targetingSourceFizzlesAfterSacrifice() {
        UUID stripMineId = harness.addToBattlefieldAndReturn(player1, new StripMine()).getId();
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, stripMineId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Strip Mine");
        harness.assertInGraveyard(player1, "Strip Mine");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
