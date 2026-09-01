package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.cards.w.WinterSky;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherStorm.class, DwarvenTrader.class, Roterothopter.class, WinterSky.class})
class AetherStormTest extends BaseCardTest {

    // ===== Creature spells can't be cast (symmetric) =====

    @Test
    @DisplayName("Controller cannot cast creature spells while Aether Storm is on the battlefield")
    void controllerCannotCastCreatureSpells() {
        harness.addToBattlefield(player1, new AetherStorm());
        harness.setHand(player1, List.of(new DwarvenTrader()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent also cannot cast creature spells (symmetric restriction)")
    void opponentAlsoCannotCastCreatureSpells() {
        harness.addToBattlefield(player1, new AetherStorm());
        harness.setHand(player2, List.of(new DwarvenTrader()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Noncreature spells can still be cast")
    void noncreatureSpellsStillCastable() {
        harness.addToBattlefield(player1, new AetherStorm());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new WinterSky(), "{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("Artifact creature spells are also prohibited")
    void artifactCreatureSpellsCannotBeCast() {
        harness.addToBattlefield(player1, new AetherStorm());
        harness.setHand(player1, List.of(new Roterothopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Losing Aether Storm's abilities removes its creature-spell restriction")
    void losingAllAbilitiesRemovesRestriction() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new AetherStorm());
        storm.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setHand(player1, List.of(new DwarvenTrader()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Pay 4 life: Destroy this enchantment =====

    @Test
    @DisplayName("Controller pays 4 life to destroy Aether Storm")
    void controllerPaysLifeToDestroy() {
        harness.addToBattlefield(player1, new AetherStorm());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertNotOnBattlefield(player1, "Aether Storm");
        harness.assertInGraveyard(player1, "Aether Storm");
    }

    @Test
    @DisplayName("The ability cannot be activated without 4 life")
    void cannotActivateWithoutEnoughLife() {
        harness.addToBattlefield(player1, new AetherStorm());
        harness.setLife(player1, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        harness.assertLife(player1, 3);
        harness.assertOnBattlefield(player1, "Aether Storm");
    }

    @Test
    @DisplayName("The destruction cannot be regenerated")
    void destructionCannotBeRegenerated() {
        Permanent storm = harness.addToBattlefieldAndReturn(player1, new AetherStorm());
        storm.setRegenerationShield(1);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aether Storm");
        harness.assertInGraveyard(player1, "Aether Storm");
    }

    @Test
    @DisplayName("Destroying Aether Storm restores the ability to cast creature spells")
    void destroyingRestoresCreatureCasting() {
        harness.addToBattlefield(player1, new AetherStorm());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DwarvenTrader()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Any player may activate =====

    @Test
    @DisplayName("An opponent may activate the ability, paying 4 life from their own total")
    void opponentMayActivate() {
        harness.addToBattlefield(player1, new AetherStorm());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Aether Storm");
    }
}
