package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RepulsorBots.class, GrizzlyBears.class, Spellbook.class, Island.class})
class RepulsorBotsTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns up to two other artifacts and/or creatures")
    void etbReturnsTwoMixedTargets() {
        UUID creatureId = addCreatureReady(player1, new GrizzlyBears()).getId();
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new Spellbook()).getId();
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new RepulsorBots(), "{4}{U}{U}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        UUID sourceId = harness.getPermanentId(player1, "Repulsor Bots");
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds())
                .contains(creatureId, artifactId)
                .doesNotContain(sourceId);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(creatureId, artifactId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Repulsor Bots");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("The ETB can return only one target")
    void etbCanReturnOneTarget() {
        UUID creatureId = addCreatureReady(player1, new GrizzlyBears()).getId();
        harness.castFromHand(player1, new RepulsorBots(), "{4}{U}{U}");
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Repulsor Bots");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB cannot target itself or a land")
    void etbExcludesSourceAndLands() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new RepulsorBots(), "{4}{U}{U}");
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Repulsor Bots");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("The ETB can choose zero targets even when legal targets exist")
    void etbCanDeclineAllTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        harness.castFromHand(player1, new RepulsorBots(), "{4}{U}{U}");
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Repulsor Bots");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Spellbook");
    }

    @Test
    @DisplayName("The ETB can stop after one target while another target remains available")
    void etbCanDeclineSecondTarget() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new Spellbook());
        harness.castFromHand(player1, new RepulsorBots(), "{4}{U}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .hasSize(2);
        harness.handleMultiplePermanentsChosen(player1, List.of(creatureId));
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertOnBattlefield(player1, "Repulsor Bots");
    }

    @Test
    @DisplayName("The ETB still returns its remaining target if the source and other target leave")
    void etbResolvesAfterSourceAndOneTargetLeave() {
        var creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID artifactId = harness.addToBattlefieldAndReturn(player2, new Spellbook()).getId();
        harness.castFromHand(player1, new RepulsorBots(), "{4}{U}{U}");
        resolveAllTriggers();

        harness.handleMultiplePermanentsChosen(player1, List.of(creature.getId(), artifactId));
        var source = findPermanent(player1, "Repulsor Bots");
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        });
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Repulsor Bots");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Spellbook");
        harness.assertNotOnBattlefield(player2, "Spellbook");
    }
}
