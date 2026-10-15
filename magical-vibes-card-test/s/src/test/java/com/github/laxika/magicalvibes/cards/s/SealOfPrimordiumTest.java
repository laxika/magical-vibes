package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.cards.k.KrakenHatchling;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealOfPrimordium.class, IzzetSignet.class, StonySilence.class, KrakenHatchling.class})
class SealOfPrimordiumTest extends BaseCardTest {

    @Test
    @DisplayName("Ability destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player1, new SealOfPrimordium());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Izzet Signet");
        harness.assertInGraveyard(player2, "Izzet Signet");
    }

    @Test
    @DisplayName("Ability destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player1, new SealOfPrimordium());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StonySilence());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Stony Silence");
        harness.assertInGraveyard(player2, "Stony Silence");
    }

    @Test
    @DisplayName("Ability can destroy an artifact controlled by its controller")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new SealOfPrimordium());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IzzetSignet());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Izzet Signet");
        harness.assertInGraveyard(player1, "Izzet Signet");
    }

    @Test
    @DisplayName("Seal of Primordium is sacrificed as a cost")
    void sacrificedAsCost() {
        harness.addToBattlefield(player1, new SealOfPrimordium());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Seal of Primordium");
        harness.assertInGraveyard(player1, "Seal of Primordium");
    }

    @Test
    @DisplayName("Ability cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player1, new SealOfPrimordium());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrakenHatchling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");

        harness.assertOnBattlefield(player1, "Seal of Primordium");
        harness.assertNotInGraveyard(player1, "Seal of Primordium");
    }

    @Test
    @DisplayName("Ability fizzles if its target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new SealOfPrimordium());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertNotInGraveyard(player2, "Izzet Signet");
        harness.assertInGraveyard(player1, "Seal of Primordium");
    }

    @Test
    @DisplayName("Seal can target itself and is sacrificed before the ability resolves")
    void canTargetItself() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new SealOfPrimordium());

        harness.activateAbility(player1, 0, null, seal.getId());

        harness.assertNotOnBattlefield(player1, "Seal of Primordium");
        harness.assertInGraveyard(player1, "Seal of Primordium");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Seal can be activated during an opponent's turn without mana")
    void canActivateTappedSealOnOpponentsTurn() {
        Permanent seal = harness.addToBattlefieldAndReturn(player1, new SealOfPrimordium());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        seal.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Seal of Primordium");
        harness.assertOnBattlefield(player2, "Izzet Signet");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Izzet Signet");
        harness.assertInGraveyard(player2, "Izzet Signet");
    }

    @Test
    @DisplayName("Activation requires a target before the sacrifice cost is paid")
    void cannotActivateWithoutTarget() {
        harness.addToBattlefield(player1, new SealOfPrimordium());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");

        harness.assertOnBattlefield(player1, "Seal of Primordium");
        harness.assertNotInGraveyard(player1, "Seal of Primordium");
        assertThat(gd.stack).isEmpty();
    }
}
