package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cindervines;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SunderShamanTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        GameData gameData = harness.getGameData();
        Permanent permanent = new Permanent(card);
        gameData.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Combat damage lets its controller destroy an artifact or enchantment controlled by the damaged player")
    void destroysArtifactOrEnchantmentControlledByDamagedPlayer() {
        Permanent shaman = addCreatureReady(player1, new SunderShaman());
        shaman.setAttacking(true);
        Permanent ownArtifact = addPermanent(player1, new FountainOfYouth());
        Permanent enemyArtifact = addPermanent(player2, new FountainOfYouth());
        Permanent enemyEnchantment = addPermanent(player2, new Cindervines());
        Permanent enemyCreature = addCreatureReady(player2, new GrizzlyBears());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(enemyArtifact.getId(), enemyEnchantment.getId())
                .doesNotContain(ownArtifact.getId(), enemyCreature.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(enemyEnchantment.getId()));

        harness.assertNotOnBattlefield(player2, "Cindervines");
        harness.assertInGraveyard(player2, "Cindervines");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("It can't be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new SunderShaman());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }
}
