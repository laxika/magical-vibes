package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoulSpirit.class, Forest.class, Mountain.class})
class FoulSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Controller with exactly one land sacrifices it automatically on ETB")
    void oneLandSacrificedAutomatically() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new FoulSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        // Creature entered, its only land was sacrificed
        harness.assertOnBattlefield(player1, "Foul Spirit");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Controller with multiple lands chooses which one to sacrifice")
    void multipleLandsControllerChooses() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new FoulSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent forest = findPermanent(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        // Chosen land gone, the other remains
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Controller with no lands still enters, unaffected")
    void noLandsUnaffected() {
        harness.setHand(player1, List.of(new FoulSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();

        harness.assertOnBattlefield(player1, "Foul Spirit");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's land does not satisfy the sacrifice requirement")
    void opponentLandDoesNotSatisfyRequirement() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new FoulSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Foul Spirit");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
