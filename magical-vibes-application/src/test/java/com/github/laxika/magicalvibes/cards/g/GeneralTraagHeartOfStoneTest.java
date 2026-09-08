package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeneralTraagHeartOfStone.class, Ornithopter.class, GrizzlyBears.class})
class GeneralTraagHeartOfStoneTest extends BaseCardTest {

    @Test
    void sacrificeAnotherArtifactDealsFourDamageToTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID artifactId = harness.getPermanentId(player1, "Ornithopter");
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new GeneralTraagHeartOfStone()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifactId);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void decliningSacrificeDealsNoDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GeneralTraagHeartOfStone()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void sourceArtifactIsNotEligibleForSacrifice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GeneralTraagHeartOfStone()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "General Traag, Heart of Stone");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
