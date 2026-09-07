package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChaosCharm;
import com.github.laxika.magicalvibes.cards.d.DwarvenDemolitionTeam;
import com.github.laxika.magicalvibes.cards.w.WordOfBlasting;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfShadows.class, ZuranSpellcaster.class, WordOfBlasting.class,
        DwarvenDemolitionTeam.class, ChaosCharm.class})
class WallOfShadowsTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from creatures it blocks")
    void preventsDamageFromBlockedCreature() {
        Permanent wall = addCreatureReady(player1, new WallOfShadows());
        Permanent attacker = addCreatureReady(player2, new ZuranSpellcaster());
        attacker.setAttacking(true);
        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        wall.addBlockingTargetId(attacker.getId());

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
        Permanent wall = addCreatureReady(player2, new WallOfShadows());
        harness.setHand(player1, List.of(new WordOfBlasting()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be targeted by an ability that can target only Walls")
    void cannotBeTargetedByWallOnlyAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new DwarvenDemolitionTeam());
        Permanent source = findPermanent(player1, "Dwarven Demolition Team");
        source.setSummoningSick(false);
        Permanent wall = addCreatureReady(player2, new WallOfShadows());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A modal spell with a broader mode may target Wall of Shadows")
    void modalSpellWithBroaderModeMayTargetIt() {
        Permanent wall = addCreatureReady(player2, new WallOfShadows());
        harness.setHand(player1, List.of(new ChaosCharm()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, 0, wall.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(wall);
    }
}
