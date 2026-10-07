package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ClayGolem;
import com.github.laxika.magicalvibes.cards.c.CommandersSphere;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoroughInvestigation.class, ClayGolem.class, CommandersSphere.class})
class ThoroughInvestigationTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, Thorough Investigation creates a Clue")
    void investigatesWhenYouAttack() {
        harness.addToBattlefield(player1, new ThoroughInvestigation());
        addCreatureReady(player1, new ClayGolem());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Sacrificing a Clue makes its controller venture into the dungeon")
    void clueSacrificeVenturesIntoDungeon() {
        investigateByAttacking(player1);
        activateClue(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertInHand(player1, "Clay Golem");
        harness.assertLife(player1, 21);
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void multipleAttackersInvestigateOnlyOnce() {
        harness.addToBattlefield(player1, new ThoroughInvestigation());
        addCreatureReady(player1, new ClayGolem());
        addCreatureReady(player1, new ClayGolem());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void declaringNoAttackersDoesNotInvestigate() {
        harness.addToBattlefield(player1, new ThoroughInvestigation());
        addCreatureReady(player1, new ClayGolem());

        declareAttackers(List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void opponentsAttackDoesNotInvestigateForYou() {
        harness.addToBattlefield(player1, new ThoroughInvestigation());
        addCreatureReady(player2, new ClayGolem());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void sacrificingClueAdvancesAnExistingDungeonAlongChosenPath() {
        investigateByAttacking(player1);
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        activateClue(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Mine Tunnels");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 2));
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void destroyingClueDoesNotVenture() {
        investigateByAttacking(player1);
        Permanent clue = findPermanent(player1, "Clue");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, clue));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerDungeonProgress).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void sacrificingNonClueDoesNotVenture() {
        harness.addToBattlefield(player1, new ThoroughInvestigation());
        harness.addToBattlefield(player1, new CommandersSphere());
        harness.setLibrary(player1, List.of(new ClayGolem()));

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Commander's Sphere");
        harness.assertInHand(player1, "Clay Golem");
        assertThat(gd.playerDungeonProgress).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentsClueSacrificeOnlyVenturesForOpponent() {
        harness.addToBattlefield(player1, new ThoroughInvestigation());
        investigateByAttacking(player2);
        activateClue(player2);
        resolveAllTriggers();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        harness.assertLife(player2, 21);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void investigateByAttacking(Player player) {
        harness.addToBattlefield(player, new ThoroughInvestigation());
        addCreatureReady(player, new ClayGolem());
        declareAttackers(player, List.of(1));
        resolveAllTriggers();
    }

    private void activateClue(Player player) {
        harness.setLibrary(player, List.of(new ClayGolem()));
        harness.addMana(player, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player.getId())
                .indexOf(findPermanent(player, "Clue"));
        harness.activateAbility(player, clueIndex, null, null);
    }
}
