package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChaosCharm;
import com.github.laxika.magicalvibes.cards.d.DAvenantArcher;
import com.github.laxika.magicalvibes.cards.d.DwarvenDemolitionTeam;
import com.github.laxika.magicalvibes.cards.g.GlyphOfDoom;
import com.github.laxika.magicalvibes.cards.p.PsychicPurge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfShadows.class, DAvenantArcher.class, GlyphOfDoom.class,
        PsychicPurge.class, DwarvenDemolitionTeam.class, ChaosCharm.class})
class WallOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from creatures it blocks")
    void preventsCombatDamageFromBlockedCreature() {
        addCreatureReady(player1, new DAvenantArcher());
        Permanent wall = addCreatureReady(player2, new WallOfShadows());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents noncombat damage from a creature it blocks")
    void preventsNoncombatDamageFromBlockedCreature() {
        Permanent wall = addCreatureReady(player1, new WallOfShadows());
        Permanent archer = addCreatureReady(player2, new DAvenantArcher());
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        wall.addBlockingTargetId(archer.getId());

        harness.activateAbility(player2, 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wall);
        assertThat(wall.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent damage from a creature it does not block")
    void doesNotPreventDamageFromOtherCreature() {
        Permanent wall = addCreatureReady(player2, new WallOfShadows());
        addCreatureReady(player1, new DAvenantArcher());
        Permanent blockedArcher = addCreatureReady(player1, new DAvenantArcher());
        wall.setBlocking(true);
        wall.addBlockingTarget(1);
        wall.addBlockingTargetId(blockedArcher.getId());

        harness.activateAbility(player1, 0, null, wall.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Wall of Shadows");
    }

    @Test
    @DisplayName("Does not prevent damage from a noncreature source")
    void doesNotPreventDamageFromNoncreatureSource() {
        Permanent wall = addCreatureReady(player2, new WallOfShadows());
        harness.setHand(player1, List.of(new PsychicPurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, wall.getId());

        harness.assertNotOnBattlefield(player2, "Wall of Shadows");
    }

    @Test
    @DisplayName("Cannot be targeted by a spell that can target only Walls")
    void cannotBeTargetedByWallOnlySpell() {
        Permanent wall = addCreatureReady(player2, new WallOfShadows());
        harness.setHand(player1, List.of(new GlyphOfDoom()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be targeted by an ability that can target only Walls")
    void cannotBeTargetedByWallOnlyAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        addCreatureReady(player1, new DwarvenDemolitionTeam());
        Permanent wall = addCreatureReady(player2, new WallOfShadows());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A modal spell with a broader mode may target Wall of Shadows")
    void modalSpellWithBroaderModeMayTargetIt() {
        Permanent wall = addCreatureReady(player2, new WallOfShadows());
        harness.setHand(player1, List.of(new ChaosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, 0, List.of(wall.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wall);
    }
}
