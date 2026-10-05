package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantSmite.class, AirElemental.class, GrizzlyBears.class})
class RadiantSmiteTest extends BaseCardTest {

    @Test
    void startingPlayerDestroysCreatureWithoutGainingLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        gd.startingPlayerId = player1.getId();

        cast(player1, target);

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertLife(player1, 20);
    }

    @Test
    void nonStartingPlayerDestroysCreatureAndGainsFourLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        gd.startingPlayerId = player1.getId();

        cast(player2, target);

        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertLife(player2, 24);
    }

    @Test
    void cannotTargetCreatureWithPowerLessThanFour() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RadiantSmite()));
        addSpellMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    void cyclingAsNonStartingPlayerGainsTwoLifeAndDraws() {
        gd.startingPlayerId = player1.getId();
        harness.setHand(player2, List.of(new RadiantSmite()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player2, "Radiant Smite");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void cyclingAsStartingPlayerDoesNotGainLife() {
        gd.startingPlayerId = player1.getId();
        harness.setHand(player1, List.of(new RadiantSmite()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Radiant Smite");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void startingPlayersCyclingDoesNotPutLifeGainTriggerOnStack() {
        gd.startingPlayerId = player1.getId();
        harness.setHand(player1, List.of(new RadiantSmite()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    void nonStartingPlayersCyclingLifeGainResolvesBeforeDraw() {
        gd.startingPlayerId = player1.getId();
        harness.setHand(player2, List.of(new RadiantSmite()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateHandAbility(player2, 0, null);

        assertThat(gd.stack).hasSize(2);
        harness.assertInGraveyard(player2, "Radiant Smite");
        harness.passBothPriorities();
        harness.assertLife(player2, 22);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void targetDroppingBelowFourPowerPreventsDestructionAndLifeGain() {
        gd.startingPlayerId = player1.getId();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player2, List.of(new RadiantSmite()));
        addSpellMana(player2);

        harness.castInstant(player2, 0, target.getId());
        target.setPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player2, "Radiant Smite");
        harness.assertLife(player2, 20);
    }

    private void cast(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        gd.activePlayerId = caster.getId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new RadiantSmite()));
        addSpellMana(caster);

        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private void addSpellMana(com.github.laxika.magicalvibes.model.Player caster) {
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
    }
}
