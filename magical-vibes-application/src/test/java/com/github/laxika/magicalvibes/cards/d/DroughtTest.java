package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FrozenShade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DroughtTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying {W}{W} at upkeep keeps Drought")
    void payAtUpkeepKeepsIt() {
        harness.addToBattlefield(player1, new Drought());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Drought");
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices Drought")
    void declineAtUpkeepSacrificesIt() {
        harness.addToBattlefield(player1, new Drought());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Drought");
    }

    @Test
    @DisplayName("Casting a black spell requires sacrificing a Swamp")
    void blackSpellRequiresSwampSacrifice() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new Swamp());
        Permanent swamp = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithImposedSacrifice(player1, 0, List.of(swamp.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Scathe Zombies");
        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Casting a black spell without a Swamp fails")
    void blackSpellWithoutSwampFails() {
        harness.addToBattlefield(player2, new Drought());
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithImposedSacrifice(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice");
    }

    @Test
    @DisplayName("Non-black spells cast without a Swamp sacrifice")
    void nonBlackSpellUnaffected() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Activating a {B} ability requires sacrificing a Swamp")
    void blackAbilityRequiresSwampSacrifice() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new FrozenShade());
        Permanent shade = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        // Exactly one Swamp → auto-pays the imposed sacrifice
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, shade)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Activating a {B} ability without a Swamp fails")
    void blackAbilityWithoutSwampFails() {
        harness.addToBattlefield(player2, new Drought());
        harness.addToBattlefield(player1, new FrozenShade());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
