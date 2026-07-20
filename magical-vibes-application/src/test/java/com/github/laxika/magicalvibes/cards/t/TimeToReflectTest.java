package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeToReflectTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature that blocked a Zombie this turn")
    void exilesCreatureThatBlockedAZombie() {
        Permanent zombie = addReady(player1, new ScatheZombies());
        zombie.setAttacking(true);
        addReady(player2, new GrizzlyBears()); // non-Zombie blocker

        declareBlock();

        UUID blockerId = harness.getPermanentId(player2, "Grizzly Bears");
        castTimeToReflect(player1, blockerId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles a creature that was blocked by a Zombie this turn")
    void exilesCreatureThatWasBlockedByAZombie() {
        Permanent attacker = addReady(player1, new GrizzlyBears()); // non-Zombie attacker
        attacker.setAttacking(true);
        addReady(player2, new ScatheZombies()); // Zombie blocker

        declareBlock();

        UUID attackerId = harness.getPermanentId(player1, "Grizzly Bears");
        castTimeToReflect(player1, attackerId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature that only blocked a non-Zombie")
    void cannotTargetCreatureThatBlockedNonZombie() {
        Permanent attacker = addReady(player1, new GrizzlyBears()); // non-Zombie attacker
        attacker.setAttacking(true);
        addReady(player2, new GiantSpider()); // non-Zombie blocker

        declareBlock();

        UUID blockerId = harness.getPermanentId(player2, "Giant Spider");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new TimeToReflect()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blockerId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void castTimeToReflect(Player caster, UUID targetId) {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(caster, List.of(new TimeToReflect()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.castInstant(caster, 0, targetId);
    }
}
