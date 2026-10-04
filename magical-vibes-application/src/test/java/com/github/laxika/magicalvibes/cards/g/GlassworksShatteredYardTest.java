package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.OverlordOfTheBoilerbilges;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlassworksShatteredYard.class, OverlordOfTheBoilerbilges.class})
class GlassworksShatteredYardTest extends BaseCardTest {

    @Test
    void glassworksDealsFourDamageToATargetCreatureAnOpponentControls() {
        Permanent target = addCreatureReady(player2, new OverlordOfTheBoilerbilges());

        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void glassworksCannotTargetACreatureItsControllerControls() {
        Permanent ownCreature = addCreatureReady(player1, new OverlordOfTheBoilerbilges());
        addCreatureReady(player2, new OverlordOfTheBoilerbilges());

        castRoom(0);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void shatteredYardDealsOneDamageToEachOpponentAtYourEndStep() {
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());
        castRoom(1);

        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 1);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new GlassworksShatteredYard()));
        harness.addMana(player1, ManaColor.RED, doorIndex == 0 ? 3 : 5);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ROOM))
                .findFirst().orElseThrow();
    }

    private void forceEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    void lockedShatteredYardDoesNotDealDamageAtYourEndStep() {
        Permanent target = addCreatureReady(player2, new OverlordOfTheBoilerbilges());
        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        int opponentLife = gd.getLife(player2.getId());

        forceEndStep(player1);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void castingShatteredYardDoesNotTriggerLockedGlassworks() {
        Permanent target = addCreatureReady(player2, new OverlordOfTheBoilerbilges());

        castRoom(1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void unlockingGlassworksAfterCastingShatteredYardDealsFourDamage() {
        Permanent target = addCreatureReady(player2, new OverlordOfTheBoilerbilges());
        castRoom(1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.unlockRoomDoor(player1, 0, 0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void unlockingShatteredYardEnablesEndStepDamageWithoutRetriggeringGlassworks() {
        Permanent target = addCreatureReady(player2, new OverlordOfTheBoilerbilges());
        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.RED, 5);
        int opponentLife = gd.getLife(player2.getId());

        harness.unlockRoomDoor(player1, 0, 1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        forceEndStep(player1);
        resolveAllTriggers();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
    }

    @Test
    void shatteredYardDoesNotTriggerAtAnOpponentsEndStep() {
        castRoom(1);
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        forceEndStep(player2);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    @Test
    void glassworksDoesNotDamageATargetThatMovesUnderItsControllersControl() {
        Permanent target = addCreatureReady(player2, new OverlordOfTheBoilerbilges());
        castRoom(0);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isZero();
    }
}
