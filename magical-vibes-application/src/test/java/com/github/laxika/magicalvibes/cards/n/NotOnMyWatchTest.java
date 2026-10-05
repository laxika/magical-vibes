package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GravestoneStrider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NotOnMyWatch.class, GravestoneStrider.class})
class NotOnMyWatchTest extends BaseCardTest {

    @Test
    void exilesTargetAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new GravestoneStrider());
        attacker.setAttacking(true);
        prepareCasting();
        harness.castAndResolveInstant(player2, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Gravestone Strider");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gravestone Strider"));
    }

    @Test
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player1, new GravestoneStrider());
        prepareCasting();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    void cannotTargetBlockingCreature() {
        Permanent blocker = addCreatureReady(player1, new GravestoneStrider());
        blocker.setBlocking(true);
        prepareCasting();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, blocker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    @Test
    void doesNotExileCreatureThatStopsAttackingBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new GravestoneStrider());
        attacker.setAttacking(true);
        castNotOnMyWatch(attacker);

        attacker.clearCombatState();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Not on My Watch");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesBlockedAttackerWithoutExilingItsBlocker() {
        Permanent attacker = addCreatureReady(player1, new GravestoneStrider());
        Permanent blocker = addCreatureReady(player2, new GravestoneStrider());
        attacker.setAttacking(true);
        attacker.setTapped(true);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(attacker.getId());
        prepareCasting();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(attacker.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void canExileOwnAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new GravestoneStrider());
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NotOnMyWatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(attacker.getCard());
    }

    private void castNotOnMyWatch(Permanent target) {
        prepareCasting();
        harness.castInstant(player2, 0, target.getId());
    }

    private void prepareCasting() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new NotOnMyWatch()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);
    }
}
