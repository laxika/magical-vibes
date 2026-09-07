package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
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
        harness.setHand(player1, List.of(dromoka));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
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
        harness.clearPriorityPassed();
        harness.passPriority(player1);

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
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }
}
