package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlexiosDeimosOfKosmos;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WoebringerDemon.class, Watchwolf.class, AlexiosDeimosOfKosmos.class})
class WoebringerDemonTest extends BaseCardTest {

    @Test
    @DisplayName("At each player's upkeep, that player sacrifices a creature")
    void activePlayerSacrificesTheirCreature() {
        harness.addToBattlefield(player1, new WoebringerDemon());
        harness.addToBattlefield(player2, new Watchwolf());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Watchwolf");
        harness.assertOnBattlefield(player1, "Woebringer Demon");
    }

    @Test
    @DisplayName("If the active player controls no creatures, Woebringer Demon is sacrificed")
    void sacrificesSelfWhenActivePlayerControlsNoCreature() {
        harness.addToBattlefield(player1, new WoebringerDemon());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Woebringer Demon");
        harness.assertInGraveyard(player1, "Woebringer Demon");
    }

    @Test
    @DisplayName("The active player chooses which creature to sacrifice")
    void activePlayerChoosesCreature() {
        harness.addToBattlefield(player1, new WoebringerDemon());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Watchwolf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        UUID chosen = second.getId();
        harness.handlePermanentChosen(player1, chosen);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(chosen));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId()));
        harness.assertOnBattlefield(player1, "Woebringer Demon");
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Woebringer Demon")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new WoebringerDemon());
        addCreatureReady(player2, new Watchwolf());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("A creature that cannot be sacrificed does not satisfy the upkeep sacrifice")
    void cannotSacrificeCreatureFallsBackToDemon() {
        harness.addToBattlefield(player2, new WoebringerDemon());
        addCreatureReady(player1, new AlexiosDeimosOfKosmos());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Alexios, Deimos of Kosmos");
        harness.assertNotOnBattlefield(player2, "Woebringer Demon");
        harness.assertInGraveyard(player2, "Woebringer Demon");
    }

    @Test
    @DisplayName("The active player may choose Woebringer Demon itself")
    void activePlayerMayChooseDemonItself() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new WoebringerDemon());
        harness.addToBattlefield(player1, new Watchwolf());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class))
                .isNotNull();
        harness.handlePermanentChosen(player1, demon.getId());

        harness.assertNotOnBattlefield(player1, "Woebringer Demon");
        harness.assertOnBattlefield(player1, "Watchwolf");
    }
}
