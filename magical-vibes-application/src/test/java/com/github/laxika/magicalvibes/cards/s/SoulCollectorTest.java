package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulCollector.class, CloudSprite.class, Shock.class})
class SoulCollectorTest extends BaseCardTest {

    @Test
    void returnsCreatureItDamagedToTheBattlefieldUnderItsControl() {
        destroyCloudSpriteInCombat();

        harness.assertOnBattlefield(player1, "Cloud Sprite");
        harness.assertNotInGraveyard(player2, "Cloud Sprite");
    }

    @Test
    void doesNotReturnCreatureItDidNotDamage() {
        harness.addToBattlefield(player1, new SoulCollector());
        harness.addToBattlefield(player2, new CloudSprite());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Cloud Sprite"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cloud Sprite");
        harness.assertNotOnBattlefield(player1, "Cloud Sprite");
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new SoulCollector()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent soulCollector = findPermanent(player1, "Soul Collector");
        assertThat(soulCollector.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(soulCollector));
        harness.passBothPriorities();

        assertThat(soulCollector.isFaceDown()).isFalse();
    }

    private void destroyCloudSpriteInCombat() {
        harness.addToBattlefield(player1, new SoulCollector());
        harness.addToBattlefield(player2, new CloudSprite());

        Permanent soulCollector = gd.playerBattlefields.get(player1.getId()).getFirst();
        soulCollector.setSummoningSick(false);
        soulCollector.setAttacking(true);

        Permanent cloudSprite = gd.playerBattlefields.get(player2.getId()).getFirst();
        cloudSprite.setSummoningSick(false);
        cloudSprite.setBlocking(true);
        cloudSprite.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
