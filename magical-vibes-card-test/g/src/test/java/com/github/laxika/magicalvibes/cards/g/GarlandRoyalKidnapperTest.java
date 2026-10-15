package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.ThornOfTheBlackRose;
import com.github.laxika.magicalvibes.cards.t.Threaten;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarlandRoyalKidnapper.class, GrizzlyBears.class, ThornOfTheBlackRose.class, Threaten.class})
class GarlandRoyalKidnapperTest extends BaseCardTest {

    @Test
    void makesTargetOpponentMonarchWhenItEnters() {
        castGarland(player1, player2.getId());

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void gainsControlOfMonarchsCreatureUntilTheyStopBeingMonarch() {
        harness.addToBattlefield(player1, new GarlandRoyalKidnapper());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.monarchPlayerId = player1.getId();

        harness.enterBattlefieldAndReturn(player2, new ThornOfTheBlackRose());
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, bearsId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.enterBattlefieldAndReturn(player1, new ThornOfTheBlackRose());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void buffsAndProtectsCreaturesControlledButNotOwned() {
        harness.addToBattlefield(player2, new GarlandRoyalKidnapper());
        UUID garlandId = harness.getPermanentId(player2, "Garland, Royal Kidnapper");

        castThreaten(player1, garlandId);

        Permanent stolenGarland = findPermanent(player1, "Garland, Royal Kidnapper");
        assertThat(gqs.getEffectivePower(gd, stolenGarland)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, stolenGarland)).isEqualTo(6);
        assertThat(gqs.cantBeSacrificed(gd, stolenGarland)).isTrue();
    }

    private void castGarland(com.github.laxika.magicalvibes.model.Player player, UUID targetPlayerId) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new GarlandRoyalKidnapper()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0, targetPlayerId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castThreaten(com.github.laxika.magicalvibes.model.Player player, UUID targetId) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new Threaten()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castSorcery(player, 0, targetId);
        harness.passBothPriorities();
    }
}
