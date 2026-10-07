package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BasiliskGate;
import com.github.laxika.magicalvibes.cards.d.DreadWanderer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerryPinTurboturtle.class, BasiliskGate.class, GrizzlyBears.class, Humble.class, DreadWanderer.class})
class TerryPinTurboturtleTest extends BaseCardTest {

    @Test
    @DisplayName("Sorcery-speed activated abilities can be activated at instant speed")
    void removesSorcerySpeedRestrictionFromActivatedAbilities() {
        harness.addToBattlefield(player1, new TerryPinTurboturtle());
        harness.addToBattlefield(player1, new BasiliskGate());
        Permanent target = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @CardUsed({TerryPinTurboturtle.class})
    void canBeCastDuringOpponentsTurn() {
        TerryPinTurboturtle terry = new TerryPinTurboturtle();
        harness.setHand(player1, List.of(terry));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(terry.getId()));
    }

    @Test
    @CardUsed({TerryPinTurboturtle.class})
    void canAttackWhileSummoningSick() {
        Permanent terry = harness.addToBattlefieldAndReturn(player1, new TerryPinTurboturtle());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(terry.isAttacking()).isTrue();
    }

    @Test
    @CardUsed({TerryPinTurboturtle.class, BasiliskGate.class})
    void doesNotGrantPermissionToOpponent() {
        Permanent terry = harness.addToBattlefieldAndReturn(player1, new TerryPinTurboturtle());
        harness.addToBattlefield(player2, new BasiliskGate());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, terry.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @CardUsed({TerryPinTurboturtle.class, BasiliskGate.class, Humble.class})
    void permissionEndsWhenTerryLosesAbilities() {
        Permanent terry = harness.addToBattlefieldAndReturn(player1, new TerryPinTurboturtle());
        harness.addToBattlefield(player1, new BasiliskGate());
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, terry.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, terry.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @CardUsed({TerryPinTurboturtle.class, DreadWanderer.class})
    void permitsSorceryRestrictedGraveyardAbilityDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TerryPinTurboturtle());
        DreadWanderer wanderer = new DreadWanderer();
        harness.setGraveyard(player1, List.of(wanderer));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(wanderer);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(wanderer.getId()));
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
