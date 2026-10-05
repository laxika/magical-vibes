package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProfessionalWrestler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingLizards.class, AirElemental.class, GrizzlyBears.class, ProfessionalWrestler.class})
class LurkingLizardsTest extends BaseCardTest {

    @Test
    void castingSpellWithManaValueFourOrGreaterPutsCounterOnLurkingLizards() {
        harness.addToBattlefield(player1, new LurkingLizards());

        Permanent lizards = findPermanent(player1, "Lurking Lizards");
        harness.castFromHand(player1, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingSpellWithManaValueLessThanFourDoesNotPutCounterOnLurkingLizards() {
        harness.addToBattlefield(player1, new LurkingLizards());

        Permanent lizards = findPermanent(player1, "Lurking Lizards");
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentCastingSpellDoesNotPutCounterOnLurkingLizards() {
        harness.addToBattlefield(player1, new LurkingLizards());
        harness.forceActivePlayer(player2);

        Permanent lizards = findPermanent(player1, "Lurking Lizards");
        harness.castFromHand(player2, new AirElemental(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void manaValueFourTriggersBeforeTheCreatureSpellResolves() {
        Permanent lizards = harness.addToBattlefieldAndReturn(player1, new LurkingLizards());

        harness.castFromHand(player1, new ProfessionalWrestler(), "{3}{G}");

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(lizards.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof ProfessionalWrestler);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void eachLizardGetsItsOwnCounterForTheSameSpell() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LurkingLizards());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LurkingLizards());

        harness.castFromHand(player1, new ProfessionalWrestler(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
