package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.q.QuickStudy;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillScionOfPeace.class, ZuranOrb.class, Forest.class, RaiseTheAlarm.class,
        QuickStudy.class, LightningStrike.class})
class WillScionOfPeaceTest extends BaseCardTest {

    @Test
    void reducesWhiteSpellByLifeGainedThisTurn() {
        prepareWillWithTwoLifeGained();

        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesBlueSpellByLifeGainedThisTurn() {
        prepareWillWithTwoLifeGained();

        harness.setHand(player1, List.of(new QuickStudy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceRedSpell() {
        prepareWillWithTwoLifeGained();

        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareWillWithTwoLifeGained() {
        addCreatureReady(player1, new WillScionOfPeace());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);
        prepareMainPhase();

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
