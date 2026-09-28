package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RescueTheFoal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PegasusGuardian.class, RescueTheFoal.class, GrizzlyBears.class})
class PegasusGuardianTest extends BaseCardTest {

    @Test
    void createsPegasusAtEndStepAfterPermanentYouControlledLeft() {
        harness.addToBattlefield(player1, new PegasusGuardian());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalTargetId = target.getId();
        harness.castAdventure(player1, 0, originalTargetId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(originalTargetId);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pegasus");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void doesNotCreatePegasusWithoutAQualifyingPermanentLeaving() {
        harness.addToBattlefield(player1, new PegasusGuardian());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pegasus");
    }

    @Test
    void rescueTheFoalTargetsOnlyCreatureYouControl() {
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        PegasusGuardian card = new PegasusGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pegasus Guardian");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
