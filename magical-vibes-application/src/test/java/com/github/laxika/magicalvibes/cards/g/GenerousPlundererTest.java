package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GenerousPlunderer.class})
class GenerousPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the upkeep trigger creates an untapped Treasure and targets only an opponent")
    void upkeepCreatesTreasuresForControllerAndOpponent() {
        addCreatureReady(player1, new GenerousPlunderer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Treasure"))
                .hasSize(1)
                .first()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isFalse());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Treasure"))
                .hasSize(1)
                .first()
                .satisfies(treasure -> assertThat(treasure.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Declining the upkeep trigger creates no Treasure tokens")
    void decliningUpkeepTriggerCreatesNothing() {
        addCreatureReady(player1, new GenerousPlunderer());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Treasure"));
    }

    @Test
    @DisplayName("Attacking deals damage equal to the defending player's artifact count")
    void attackDamagesDefendingPlayerForArtifactCount() {
        addCreatureReady(player1, new GenerousPlunderer());
        addArtifact(player1, "Controller Artifact");
        addArtifact(player1, "Another Controller Artifact");
        addArtifact(player2, "Defending Artifact");
        addArtifact(player2, "Another Defending Artifact");

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    private void addArtifact(Player player, String name) {
        Card artifact = new Card();
        artifact.setName(name);
        artifact.setType(CardType.ARTIFACT);
        artifact.setToken(true);
        Permanent permanent = new Permanent(artifact);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
