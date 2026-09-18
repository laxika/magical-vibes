package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExaltedAngel.class, GlorySeeker.class})
class ExaltedAngelTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new ExaltedAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Exalted Angel");
        assertThat(angel.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(angel));
        harness.passBothPriorities();

        assertThat(angel.isFaceDown()).isFalse();
    }

    @Test
    void gainsLifeEqualToDamageDealt() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        ExaltedAngel card = new ExaltedAngel();
        card.setPower(4);
        Permanent angel = addCreatureReady(player1, card);
        angel.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 16);
    }

    @Test
    void stillGainsLifeFromDamageToABlockerWhenItDies() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        ExaltedAngel card = new ExaltedAngel();
        card.setPower(4);
        card.setToughness(1);
        Permanent angel = addCreatureReady(player1, card);
        angel.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GlorySeeker());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Exalted Angel");
        harness.assertLife(player1, 14);
    }

    @Test
    void doesNotGainLifeWhenItDealsNoDamage() {
        harness.setLife(player1, 10);

        ExaltedAngel card = new ExaltedAngel();
        card.setPower(0);
        Permanent angel = addCreatureReady(player1, card);
        angel.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }
}
