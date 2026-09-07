package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoebringerDemon.class, GrizzlyBears.class})
class WoebringerDemonTest extends BaseCardTest {

    @Test
    @DisplayName("At each player's upkeep, that player sacrifices a creature")
    void activePlayerSacrificesTheirCreature() {
        harness.addToBattlefield(player1, new WoebringerDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
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
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
}
