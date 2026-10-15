package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncestorsProphet;
import com.github.laxika.magicalvibes.cards.f.FallenCleric;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarlitSanctum.class, AncestorsProphet.class, FallenCleric.class, GlorySeeker.class})
@DisplayName("Starlit Sanctum")
class StarlitSanctumTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new StarlitSanctum());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(sanctum.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate a tap ability while the Sanctum is tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new StarlitSanctum());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains life equal to the sacrificed Cleric's toughness")
    void gainsLifeEqualToSacrificedClericToughness() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, new AncestorsProphet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareAbility();

        harness.setLife(player1, 10);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Ancestor's Prophet");
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Makes a target player lose life equal to the sacrificed Cleric's power")
    void targetPlayerLosesLifeEqualToSacrificedClericPower() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, new FallenCleric());
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareAbility();

        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.assertInGraveyard(player1, "Fallen Cleric");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Can target its controller with the life-loss ability")
    void targetPlayerCanBeItsController() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, new FallenCleric());
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareAbility();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Cannot pay a Cleric sacrifice cost with a non-Cleric creature")
    void cannotSacrificeNonClericCreature() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, new GlorySeeker());
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses final toughness including counters and does not subtract marked damage")
    void gainsLifeUsingModifiedToughness() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new StarlitSanctum());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new AncestorsProphet());
        cleric.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        cleric.setMarkedDamage(3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);
        prepareAbility();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(sanctum.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Ancestor's Prophet");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Uses final power including counters when paying the sacrifice cost")
    void losesLifeUsingModifiedPower() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        cleric.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);
        prepareAbility();

        harness.activateAbility(player1, 0, 2, null, player2.getId());

        harness.assertInGraveyard(player1, "Fallen Cleric");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Life gain uses the toughness of the Cleric chosen among multiple Clerics")
    void gainsLifeFromChosenCleric() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        harness.addToBattlefield(player1, new AncestorsProphet());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new FallenCleric());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 10);
        prepareAbility();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertInGraveyard(player1, "Fallen Cleric");
        harness.assertOnBattlefield(player1, "Ancestor's Prophet");
    }

    @Test
    @DisplayName("Life loss uses the power of the Cleric chosen among multiple Clerics")
    void losesLifeFromChosenCleric() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new AncestorsProphet());
        harness.addToBattlefield(player1, new FallenCleric());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);
        prepareAbility();

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Ancestor's Prophet");
        harness.assertOnBattlefield(player1, "Fallen Cleric");
    }

    @Test
    @DisplayName("A Cleric with negative power causes no life loss or life gain")
    void negativePowerCausesNoLifeChange() {
        harness.addToBattlefield(player1, new StarlitSanctum());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new AncestorsProphet());
        cleric.setPowerModifier(-3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player2, 20);
        prepareAbility();

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Ancestor's Prophet");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Cleric")
    void cannotSacrificeOpponentsCleric() {
        Permanent sanctum = harness.addToBattlefieldAndReturn(player1, new StarlitSanctum());
        harness.addToBattlefield(player2, new AncestorsProphet());
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sanctum.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Ancestor's Prophet");
        assertThat(gd.stack).isEmpty();
    }
    private void prepareAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
