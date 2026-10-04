package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HellkiteTyrant.class, GrizzlyBears.class, LeoninScimitar.class, Ornithopter.class, Naturalize.class})
class HellkiteTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player steals every artifact that player controls")
    void combatDamageStealsArtifacts() {
        addCreatureReady(player1, new HellkiteTyrant());
        Permanent scimitar = addCreatureReady(player2, new LeoninScimitar());
        Permanent thopter = addCreatureReady(player2, new Ornithopter());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        resolveCombatUnblocked();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(scimitar.getId()))
                .anyMatch(p -> p.getId().equals(thopter.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(scimitar.getId()))
                .noneMatch(p -> p.getId().equals(thopter.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("No artifacts to steal leaves the damaged player's board untouched")
    void combatDamageWithNoArtifacts() {
        addCreatureReady(player1, new HellkiteTyrant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        resolveCombatUnblocked();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Wins the game at upkeep while controlling twenty artifacts")
    void winsWithTwentyArtifacts() {
        harness.addToBattlefield(player1, new HellkiteTyrant());
        addArtifacts(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with only nineteen artifacts")
    void doesNotTriggerWithNineteenArtifacts() {
        harness.addToBattlefield(player1, new HellkiteTyrant());
        addArtifacts(player1, 19);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent's artifacts do not count toward the win condition")
    void opponentArtifactsDoNotCount() {
        harness.addToBattlefield(player1, new HellkiteTyrant());
        addArtifacts(player1, 10);
        addArtifacts(player2, 10);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Losing an artifact in response prevents the upkeep win")
    void rechecksArtifactCountOnResolution() {
        harness.addToBattlefield(player1, new HellkiteTyrant());
        addArtifacts(player1, 20);
        Permanent artifact = findPermanent(player1, "Leonin Scimitar");

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Leonin Scimitar")).isEqualTo(19);
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Twenty artifacts do not cause a win during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new HellkiteTyrant());
        addArtifacts(player1, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("More than twenty artifacts also satisfy the upkeep win condition")
    void winsWithMoreThanTwentyArtifacts() {
        harness.addToBattlefield(player1, new HellkiteTyrant());
        addArtifacts(player1, 21);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    /** Declares no blockers, resolves combat damage, then resolves the trigger it puts on the stack. */
    private void resolveCombatUnblocked() {
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addCreatureReady(player, new LeoninScimitar());
        }
    }
}
