package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArlinnVoiceOfThePack.class, AvianChangeling.class, GrizzlyBears.class,
        GreaterWerewolf.class, YoungWolf.class})
class ArlinnVoiceOfThePackTest extends BaseCardTest {

    @Test
    @DisplayName("Wolf and Werewolf creatures you control enter with an additional +1/+1 counter")
    void wolfAndWerewolfCreaturesEnterWithCounters() {
        addReadyArlinn(player1);

        castCreature(new YoungWolf(), "{G}");
        castCreature(new GreaterWerewolf(), "{4}{B}");
        castCreature(new GrizzlyBears(), "{1}{G}");

        assertThat(findPermanent(player1, "Young Wolf")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Greater Werewolf")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature that is both a Wolf and a Werewolf gets only one counter")
    void creatureMatchingBothSubtypesGetsOneCounter() {
        addReadyArlinn(player1);

        castCreature(new AvianChangeling(), "{2}{W}");

        assertThat(findPermanent(player1, "Avian Changeling")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability creates a green Wolf token that also gets the static counter")
    void minusTwoCreatesWolfTokenWithCounter() {
        Permanent arlinn = addReadyArlinn(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Wolf");
        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The static ability does not affect an opponent's Wolf")
    void opponentWolfDoesNotGetCounter() {
        addReadyArlinn(player1);

        harness.addToBattlefield(player2, new YoungWolf());

        assertThat(findPermanent(player2, "Young Wolf")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyArlinn(Player player) {
        Permanent arlinn = new Permanent(new ArlinnVoiceOfThePack());
        arlinn.setCounterCount(CounterType.LOYALTY, 3);
        arlinn.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(arlinn);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return arlinn;
    }

    private void castCreature(com.github.laxika.magicalvibes.model.Card card, String manaCost) {
        harness.castFromHand(player1, card, manaCost);
        harness.passBothPriorities();
    }
}
