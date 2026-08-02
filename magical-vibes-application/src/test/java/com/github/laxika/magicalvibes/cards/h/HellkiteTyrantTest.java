package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HellkiteTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player steals every artifact that player controls")
    void combatDamageStealsArtifacts() {
        addCreatureReady(player1, new HellkiteTyrant());
        Permanent scimitar = addPermanent(player2, new LeoninScimitar());
        Permanent thopter = addPermanent(player2, new Ornithopter());
        Permanent bears = addPermanent(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
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
        Permanent bears = addPermanent(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
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

    /** Declares no blockers, resolves combat damage, then resolves the trigger it puts on the stack. */
    private void resolveCombatUnblocked() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addPermanent(player, new LeoninScimitar());
        }
    }
}
