package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CausticWaspsTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        GameData gameData = harness.getGameData();
        Permanent permanent = new Permanent(card);
        gameData.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Accepting the combat damage trigger destroys an artifact the damaged player controls")
    void destroysDamagedPlayersArtifact() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        Permanent artifact = addPermanent(player2, new FountainOfYouth());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(artifact.getId()));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Declining the combat damage trigger leaves the artifact on the battlefield")
    void declineLeavesArtifact() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        harness.addToBattlefield(player2, new FountainOfYouth());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Only artifacts controlled by the damaged player are legal targets")
    void onlyDamagedPlayersArtifactsAreLegalTargets() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        Permanent ownArtifact = addPermanent(player1, new FountainOfYouth());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent enemyArtifact = addPermanent(player2, new FountainOfYouth());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .containsExactly(enemyArtifact.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());
    }

    @Test
    @DisplayName("No combat damage trigger is offered when the damaged player controls no artifacts")
    void noTriggerWithoutArtifacts() {
        Permanent wasps = addCreatureReady(player1, new CausticWasps());
        wasps.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
