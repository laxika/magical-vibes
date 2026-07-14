package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AshlingTheExtinguisherTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Combat damage to a player prompts to choose a creature that player controls")
    void promptsToChooseCreature() {
        Permanent ashling = addReadyCreature(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent enemyCreature = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities(); // resolve sacrifice trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyCreature.getId());
    }

    @Test
    @DisplayName("The chosen creature is sacrificed and the game advances")
    void sacrificesChosenCreature() {
        Permanent ashling = addReadyCreature(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent enemyCreature = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(enemyCreature.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Only the damaged player's creatures are valid choices (not own creatures, not lands)")
    void onlyDamagedPlayersCreatures() {
        Permanent ashling = addReadyCreature(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent enemyCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent enemyLand = addPermanent(player2, new Mountain());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).validIds())
                .contains(enemyCreature.getId())
                .doesNotContain(ownCreature.getId())
                .doesNotContain(enemyLand.getId());
    }

    @Test
    @DisplayName("No trigger when the damaged player controls no creatures")
    void noTriggerWithoutCreatures() {
        Permanent ashling = addReadyCreature(player1, new AshlingTheExtinguisher());
        ashling.setAttacking(true);
        addPermanent(player2, new Forest());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
