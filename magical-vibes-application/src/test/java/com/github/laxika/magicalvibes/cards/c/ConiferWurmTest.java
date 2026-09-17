package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConiferWurm.class, GrizzlyBears.class})
class ConiferWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +X/+X based on snow permanents its controller controls")
    void getsBoostBasedOnSnowPermanentsYouControl() {
        Permanent wurm = addReadyWurm();
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wurm.getEffectivePower()).isEqualTo(7);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOffAtEndOfTurn() {
        Permanent wurm = addReadyWurm();
        addSnowPermanent(player1);
        addManaForAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(wurm.getEffectivePower()).isEqualTo(6);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(6);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wurm.getEffectivePower()).isEqualTo(4);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(4);
    }

    private Permanent addReadyWurm() {
        Permanent wurm = new Permanent(new ConiferWurm());
        wurm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wurm);
        return wurm;
    }

    private void addSnowPermanent(Player player) {
        Permanent snowPermanent = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(snowPermanent).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowPermanent);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
