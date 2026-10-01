package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SageOfEpityr;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FledglingMawcor.class, Island.class, SageOfEpityr.class, ChandraNalaar.class})
class FledglingMawcorTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent mawcor = addCreatureReady(player1, new FledglingMawcor());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mawcor.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to target creature")
    void deals1DamageToCreature() {
        addCreatureReady(player1, new FledglingMawcor());
        harness.addToBattlefield(player2, new SageOfEpityr());

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Sage of Epityr"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sage of Epityr");
        harness.assertInGraveyard(player2, "Sage of Epityr");
    }

    @Test
    @DisplayName("Deals 1 damage to target planeswalker")
    void deals1DamageToPlaneswalker() {
        addCreatureReady(player1, new FledglingMawcor());
        ChandraNalaar chandra = new ChandraNalaar();
        chandra.setLoyalty(6);
        Permanent planeswalker = new Permanent(chandra);
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void canBeTurnedFaceUpForItsMorphCost() {
        harness.setHand(player1, List.of(new FledglingMawcor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mawcor = findPermanent(player1, "Fledgling Mawcor");
        assertThat(mawcor.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mawcor));
        harness.passBothPriorities();

        assertThat(mawcor.isFaceDown()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new FledglingMawcor());
        harness.addToBattlefield(player2, new Island());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class);
    }
}
