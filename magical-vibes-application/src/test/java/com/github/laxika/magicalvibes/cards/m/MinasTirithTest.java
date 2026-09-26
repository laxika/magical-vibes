package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinasTirith.class, ArvadTheCursed.class, GrizzlyBears.class})
class MinasTirithTest extends BaseCardTest {

    @Test
    void entersTappedWithoutLegendaryCreature() {
        playLand();

        assertThat(findPermanent(player1, "Minas Tirith").isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        playLand();

        assertThat(findPermanent(player1, "Minas Tirith").isTapped()).isFalse();
    }

    @Test
    void tapsForWhiteMana() {
        Permanent minasTirith = addReadyMinasTirith();

        harness.activateAbility(player1, indexOf(minasTirith), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(minasTirith.isTapped()).isTrue();
    }

    @Test
    void drawsAfterAttackingWithTwoCreatures() {
        Permanent minasTirith = addReadyMinasTirith();
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(minasTirith), 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(minasTirith.isTapped()).isTrue();
    }

    @Test
    void cannotDrawWithoutAttackingWithTwoCreatures() {
        Permanent minasTirith = addReadyMinasTirith();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(minasTirith), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked with two or more creatures");
        assertThat(minasTirith.isTapped()).isFalse();
    }

    private void playLand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MinasTirith()));
        harness.playLand(player1, 0);
    }

    private Permanent addReadyMinasTirith() {
        Permanent minasTirith = new Permanent(new MinasTirith());
        minasTirith.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(minasTirith);
        return minasTirith;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
