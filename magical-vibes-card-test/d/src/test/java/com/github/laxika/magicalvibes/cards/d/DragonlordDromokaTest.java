package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonlordDromoka.class, Cancel.class, Shock.class, ProdigalSorcerer.class})
class DragonlordDromokaTest extends BaseCardTest {

    @Test
    @DisplayName("This spell cannot be countered")
    void cannotBeCountered() {
        DragonlordDromoka dromoka = new DragonlordDromoka();

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, dromoka, "{4}{G}{W}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, dromoka.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dragonlord Dromoka");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Opponents cannot cast spells during its controller's turn")
    void opponentsCannotCastDuringControllerTurn() {
        Permanent dromoka = harness.addToBattlefieldAndReturn(player1, new DragonlordDromoka());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player2);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, dromoka.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponents can cast spells during their own turn")
    void opponentsCanCastDuringOwnTurn() {
        harness.addToBattlefield(player1, new DragonlordDromoka());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("The restriction does not block opponents' activated abilities")
    void restrictionDoesNotBlockActivatedAbilities() {
        harness.addToBattlefield(player1, new DragonlordDromoka());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The controller can cast spells during their own turn")
    void controllerCanCastDuringOwnTurn() {
        harness.addToBattlefield(player1, new DragonlordDromoka());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's spell already on the stack still resolves after Dromoka enters")
    void existingSpellStillResolves() {
        harness.forceActivePlayer(player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.enterBattlefieldAndReturn(player1, new DragonlordDromoka());

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Combat damage gains life for Dromoka's controller")
    void combatDamageGainsLife() {
        Permanent dromoka = addCreatureReady(player1, new DragonlordDromoka());
        dromoka.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Dromoka")
    void groundCreatureCannotBlock() {
        Permanent dromoka = addCreatureReady(player1, new DragonlordDromoka());
        dromoka.setAttacking(true);
        addCreatureReady(player2, new ProdigalSorcerer());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
