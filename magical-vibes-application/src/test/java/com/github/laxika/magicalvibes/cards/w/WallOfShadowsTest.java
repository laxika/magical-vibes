package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChaosCharm;
import com.github.laxika.magicalvibes.cards.d.DwarvenDemolitionTeam;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WallOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from creatures it blocks")
    void preventsDamageFromCreaturesItBlocks() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        Permanent wall = addBlocker(player1, new WallOfShadows(), attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot be targeted by a spell that can target only Walls")
    void cannotBeTargetedByWallOnlySpell() {
        harness.addToBattlefield(player2, new WallOfShadows());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID wallId = harness.getPermanentId(player2, "Wall of Shadows");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wallId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Card is not playable");
    }

    @Test
    @DisplayName("Cannot be targeted by an ability that can target only Walls")
    void cannotBeTargetedByWallOnlyAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new DwarvenDemolitionTeam());
        Permanent demolitionTeam = findPermanent(player1, "Dwarven Demolition Team");
        demolitionTeam.setSummoningSick(false);
        harness.addToBattlefield(player2, new WallOfShadows());
        UUID wallId = harness.getPermanentId(player2, "Wall of Shadows");
        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(demolitionTeam);

        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, wallId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only Walls");
    }

    @Test
    @DisplayName("A modal spell with a non-Wall mode can target it")
    void modalSpellWithNonWallModeCanTargetIt() {
        harness.addToBattlefield(player2, new WallOfShadows());
        harness.setHand(player1, List.of(new ChaosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID wallId = harness.getPermanentId(player2, "Wall of Shadows");
        harness.castInstant(player1, 0, 0, wallId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Shadows");
    }

    private Permanent addAttacker(Player player, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(Player player, Card card, Permanent attacker) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(attacker.getId());
        gd.playerBattlefields.get(player.getId()).add(blocker);
        return blocker;
    }
}
