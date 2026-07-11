package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class YawgmothDemonTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    private Permanent demon(Player owner) {
        UUID id = harness.getPermanentId(owner, "Yawgmoth Demon");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Declining the sacrifice taps the Demon and deals 2 damage to its controller")
    void declineTapsAndDealsDamage() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(demon(player1).isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        // Artifact was not sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard().getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Accepting with a single artifact sacrifices it with no penalty")
    void acceptSacrificesArtifactNoPenalty() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().getName().equals("Ornithopter"));
        assertThat(demon(player1).isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Accepting with multiple artifacts prompts a choice; only the chosen one is sacrificed")
    void acceptWithMultipleArtifactsPromptsChoice() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        UUID chosenArtifact = harness.getPermanentId(player1, "Ornithopter");

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, chosenArtifact);

        long artifactsLeft = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ornithopter"))
                .count();
        assertThat(artifactsLeft).isEqualTo(1);
        assertThat(demon(player1).isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("With no artifacts, the penalty applies immediately without a prompt")
    void noArtifactsAppliesPenalty() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → penalty (no artifact to sacrifice)

        assertThat(demon(player1).isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(demon(player1).isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard().getName().equals("Ornithopter"));
    }
}
