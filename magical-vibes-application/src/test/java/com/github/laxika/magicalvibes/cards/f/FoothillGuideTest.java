package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoothillGuide.class, GoblinSledder.class, GlorySeeker.class})
class FoothillGuideTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForWhite() {
        harness.setHand(player1, List.of(new FoothillGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent guide = findPermanent(player1, "Foothill Guide");
        assertThat(guide.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guide));
        harness.passBothPriorities();

        assertThat(guide.isFaceDown()).isFalse();
    }

    @Test
    void takesNoCombatDamageFromGoblinCreature() {
        Permanent attacker = addCreatureReady(player1, new GoblinSledder());
        attacker.setAttacking(true);

        Permanent guide = addCreatureReady(player2, new FoothillGuide());
        guide.setBlocking(true);
        guide.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Foothill Guide");
        assertThat(guide.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Goblin Sledder");
    }

    @Test
    void goblinCreatureCannotBlockFoothillGuide() {
        Permanent guide = addCreatureReady(player1, new FoothillGuide());
        guide.setAttacking(true);
        addCreatureReady(player2, new GoblinSledder());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void takesNormalCombatDamageFromNonGoblinCreature() {
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        attacker.setAttacking(true);

        Permanent guide = addCreatureReady(player2, new FoothillGuide());
        guide.setBlocking(true);
        guide.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertNotOnBattlefield(player2, "Foothill Guide");
    }
}
