package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicSpite.class, GrizzlyBears.class, ChandraNalaar.class, InvasionOfInnistrad.class,
        Shock.class, HillGiant.class, Mountain.class})
class VolcanicSpiteTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToCreaturePlaneswalkerAndBattle() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);

        harness.setHand(player1, List.of(new VolcanicSpite()));
        castAt(creature);
        harness.setHand(player1, List.of(new VolcanicSpite()));
        castAt(planeswalker);
        harness.setHand(player1, List.of(new VolcanicSpite()));
        castAt(battle);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void mayPutCardOnBottomThenDraws() {
        Shock bottom = new Shock();
        Shock draw = new Shock();
        Card keep = new HillGiant();
        harness.setHand(player1, List.of(new VolcanicSpite(), bottom, keep));
        harness.setLibrary(player1, List.of(draw));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castAt(target);

        harness.handleMultipleCardsChosen(player1, List.of(bottom.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep, draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom);
    }

    @Test
    void mayDeclineToPutCardOnBottom() {
        Shock draw = new Shock();
        Card keep = new HillGiant();
        harness.setHand(player1, List.of(new VolcanicSpite(), keep));
        harness.setLibrary(player1, List.of(draw));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castAt(target);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void cannotTargetAPlayerOrLand() {
        harness.setHand(player1, List.of(new VolcanicSpite()));
        addMana();
        harness.addToBattlefield(player2, new Mountain());

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Mountain")))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(Permanent target) {
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
