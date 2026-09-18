package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DecreeOfPain;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.t.TwistedAbomination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulDead.class, TwistedAbomination.class, GoblinBrigand.class, DecreeOfPain.class})
class VengefulDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Each opponent loses 1 life when another Zombie dies")
    void triggersWhenAnotherZombieDies() {
        Permanent vengefulDead = harness.addToBattlefieldAndReturn(player1, new VengefulDead());
        vengefulDead.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player2, new TwistedAbomination());
        harness.setLife(player2, 20);

        destroyAllCreaturesWithDecree(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Vengeful Dead");
        harness.assertInGraveyard(player2, "Twisted Abomination");
    }

    @Test
    @DisplayName("A non-Zombie creature you control dying does not trigger")
    void doesNotTriggerForNonZombie() {
        Permanent vengefulDead = harness.addToBattlefieldAndReturn(player1, new VengefulDead());
        vengefulDead.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.setLife(player2, 20);

        destroyAllCreaturesWithDecree(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Vengeful Dead");
        harness.assertInGraveyard(player1, "Goblin Brigand");
    }

    @Test
    @DisplayName("The Vengeful Dead's own death triggers")
    void triggersWhenItselfDies() {
        harness.addToBattlefield(player1, new VengefulDead());
        harness.setLife(player2, 20);

        destroyAllCreaturesWithDecree(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Vengeful Dead");
    }

    @Test
    @DisplayName("Triggers for both itself and another Zombie in a simultaneous death")
    void triggersForEachZombieInSimultaneousDeath() {
        harness.addToBattlefield(player1, new VengefulDead());
        harness.addToBattlefield(player1, new VengefulDead());
        harness.setLife(player2, 20);

        destroyAllCreaturesWithDecree(player1);

        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void destroyAllCreaturesWithDecree(com.github.laxika.magicalvibes.model.Player caster) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(caster, new DecreeOfPain(), "{6}{B}{B}");
        harness.passBothPriorities();
    }
}
