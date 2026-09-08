package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarchOfTheWorldOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures become 6/6 Oozes, but an opponent's creature does not")
    void transformsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBear = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gd, ownBear)).contains(CardSubtype.OOZE);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingBear)).doesNotContain(CardSubtype.OOZE);
    }

    @Test
    @DisplayName("An opponent's spell during your turn creates an Elephant Ooze")
    void opponentSpellDuringOwnTurnCreatesToken() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        Permanent token = findPermanent(player1, "Elephant");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gameData, token)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gameData, token)).isEqualTo(6);
        assertThat(gqs.effectiveCreatureSubtypes(gameData, token))
                .contains(CardSubtype.ELEPHANT, CardSubtype.OOZE);
    }

    @Test
    @DisplayName("An opponent's spell during their own turn does not create a token")
    void opponentSpellDuringTheirTurnDoesNotCreateToken() {
        harness.addToBattlefield(player1, new MarchOfTheWorldOoze());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Elephant");
    }
}
