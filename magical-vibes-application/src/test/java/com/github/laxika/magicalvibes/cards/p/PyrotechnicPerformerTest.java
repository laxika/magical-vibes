package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MarketwatchPhantom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PyrotechnicPerformer.class, MarketwatchPhantom.class})
class PyrotechnicPerformerTest extends BaseCardTest {

    @Test
    void turningThisCreatureFaceUpDealsItsPowerToEachOpponent() {
        harness.setHand(player1, List.of(new PyrotechnicPerformer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent performer = findPermanent(player1, "Pyrotechnic Performer");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(performer));
        harness.passBothPriorities();

        assertThat(performer.isFaceDown()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    void anotherCreatureTurningFaceUpDealsThatCreaturesPowerToEachOpponent() {
        addCreatureReady(player1, new PyrotechnicPerformer());

        MarketwatchPhantom phantom = new MarketwatchPhantom();
        phantom.addMorph("{0}");
        harness.setHand(player1, List.of(phantom));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent turnedCreature = findPermanent(player1, "Marketwatch Phantom");
        int turnedCreaturePower = turnedCreature.getEffectivePower();
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(turnedCreature));
        harness.passBothPriorities();

        assertThat(turnedCreature.isFaceDown()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20 - turnedCreaturePower);
    }
}
