package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KadenasSilencer.class, IcyManipulator.class, LightningBolt.class})
class KadenasSilencerTest extends BaseCardTest {

    @Test
    void turningFaceUpCountersOpponentActivatedAbilities() {
        Permanent silencer = castFaceDownSilencer();

        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icyManipulator = findPermanent(player2, "Icy Manipulator");
        icyManipulator.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null,
                silencer.getId());

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Icy Manipulator"));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(silencer));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(silencer.isTapped()).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Icy Manipulator"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    private Permanent castFaceDownSilencer() {
        harness.setHand(player1, List.of(new KadenasSilencer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Kadena's Silencer");
    }
}
