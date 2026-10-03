package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishArchers;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuthlessWinnower.class, GrizzlyBears.class, ElvishArchers.class})
class RuthlessWinnowerTest extends BaseCardTest {

    @Test
    @DisplayName("The active player sacrifices a non-Elf creature during their upkeep")
    void activePlayerSacrificesNonElfCreature() {
        harness.addToBattlefield(player1, new RuthlessWinnower());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ruthless Winnower");
    }

    @Test
    @DisplayName("Each player sacrifices their own non-Elf creature during their upkeep")
    void eachPlayerSacrificesTheirOwnCreature() {
        harness.addToBattlefield(player1, new RuthlessWinnower());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Elves are not eligible to be sacrificed")
    void elvesAreNotSacrificed() {
        harness.addToBattlefield(player1, new RuthlessWinnower());
        harness.addToBattlefield(player1, new ElvishArchers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Archers");
    }

    @Test
    @DisplayName("The player chooses which non-Elf creature to sacrifice")
    void playerChoosesWhichNonElfCreatureToSacrifice() {
        harness.addToBattlefield(player1, new RuthlessWinnower());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishArchers());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(elf.getId()));
    }
}
