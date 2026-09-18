package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatriarchsBidding.class, ElvishWarrior.class, GoblinSledder.class, AvianChangeling.class})
class PatriarchsBiddingTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a type and returns all matching creatures from their graveyard")
    void eachPlayerChoosesTheirOwnType() {
        harness.setGraveyard(player1, List.of(new ElvishWarrior(), new ElvishWarrior(), new GoblinSledder()));
        harness.setGraveyard(player2, List.of(new GoblinSledder(), new GoblinSledder(), new ElvishWarrior()));
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "ELF");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "GOBLIN");

        assertThat(findPermanents(player1, "Elvish Warrior")).hasSize(2);
        assertThat(findPermanents(player2, "Goblin Sledder")).hasSize(2);
        harness.assertInGraveyard(player1, "Goblin Sledder");
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Changeling creature cards match each player's chosen type")
    void changelingMatchesChosenType() {
        harness.setGraveyard(player1, List.of(new AvianChangeling()));
        harness.setGraveyard(player2, List.of(new AvianChangeling()));
        cast();

        harness.handleListChoice(player1, "BEAR");
        harness.handleListChoice(player2, "GIANT");

        assertThat(findPermanents(player1, "Avian Changeling")).hasSize(1);
        assertThat(findPermanents(player2, "Avian Changeling")).hasSize(1);
    }

    @Test
    @DisplayName("No cards return when a chosen type is absent from a graveyard")
    void doesNothingWhenChosenTypesAreAbsent() {
        harness.setGraveyard(player1, List.of(new ElvishWarrior()));
        harness.setGraveyard(player2, List.of());
        cast();

        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player2, "ELF");

        harness.assertNotOnBattlefield(player1, "Elvish Warrior");
        harness.assertNotOnBattlefield(player2, "Goblin Sledder");
        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The active player chooses their creature type first")
    void activePlayerChoosesFirst() {
        harness.setGraveyard(player1, List.of(new ElvishWarrior()));
        harness.setGraveyard(player2, List.of(new GoblinSledder()));
        harness.forceActivePlayer(player2);
        cast(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "GOBLIN");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "ELF");

        harness.assertOnBattlefield(player1, "Elvish Warrior");
        harness.assertOnBattlefield(player2, "Goblin Sledder");
    }

    private void cast() {
        cast(player1);
    }

    private void cast(Player caster) {
        harness.castFromHand(caster, new PatriarchsBidding(), "{3}{B}{B}");
        harness.passBothPriorities();
    }
}
