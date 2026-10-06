package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiserysShadow.class, GrizzlyBears.class, Shock.class, Disfigure.class, DressDown.class})
class MiserysShadowTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives Misery's Shadow +1/+1 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent shadow = harness.addToBattlefieldAndReturn(player1, new MiserysShadow());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shadow.getEffectivePower()).isEqualTo(3);
        assertThat(shadow.getEffectiveToughness()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shadow.getEffectivePower()).isEqualTo(2);
        assertThat(shadow.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creature is exiled instead of going to its graveyard")
    void opponentCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new MiserysShadow());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var bearsPermId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, bearsPermId);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature controlled by Misery's Shadow's controller still goes to the graveyard")
    void ownCreatureStillDiesNormally() {
        harness.addToBattlefield(player1, new MiserysShadow());
        harness.addToBattlefield(player1, new GrizzlyBears());
        var bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bearsPermId);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Pump activations can be repeated while tapped and summoning sick")
    void pumpActivationsStackWithoutTapping() {
        Permanent shadow = harness.addToBattlefieldAndReturn(player1, new MiserysShadow());
        shadow.tap();
        shadow.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shadow.getEffectivePower()).isEqualTo(4);
        assertThat(shadow.getEffectiveToughness()).isEqualTo(4);
        assertThat(shadow.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent's creature with zero toughness is exiled")
    void zeroToughnessCreatureIsExiled() {
        harness.addToBattlefield(player1, new MiserysShadow());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new MiserysShadow());
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, victim.getId());

        harness.assertNotOnBattlefield(player2, "Misery's Shadow");
        harness.assertNotInGraveyard(player2, "Misery's Shadow");
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(victim.getCard().getId()));
    }

    @Test
    @DisplayName("Misery's Shadow does not exile itself when it dies")
    void shadowItselfDiesNormally() {
        Permanent shadow = harness.addToBattlefieldAndReturn(player1, new MiserysShadow());
        harness.setHand(player2, List.of(new Disfigure()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0, shadow.getId());

        harness.assertInGraveyard(player1, "Misery's Shadow");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Losing abilities disables the exile replacement")
    void abilityLossDisablesExileReplacement() {
        harness.addToBattlefield(player1, new MiserysShadow());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new MiserysShadow());
        harness.addToBattlefield(player1, new DressDown());
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, victim.getId());

        harness.assertInGraveyard(player2, "Misery's Shadow");
        assertThat(gd.exiledCards).isEmpty();
    }
}
