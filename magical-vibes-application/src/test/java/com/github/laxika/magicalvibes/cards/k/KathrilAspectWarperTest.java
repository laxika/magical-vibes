package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AkromaAngelOfWrath;
import com.github.laxika.magicalvibes.cards.g.GoblinDarkDwellers;
import com.github.laxika.magicalvibes.cards.n.NyxWeaver;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogbonder;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.cards.z.ZetalpaPrimalDawn;
import com.github.laxika.magicalvibes.cards.m.MireTriton;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KathrilAspectWarper.class, GrizzlyBears.class, MireTriton.class, SerraAngel.class,
        AkromaAngelOfWrath.class, GoblinDarkDwellers.class, NyxWeaver.class,
        SlipperyBogbonder.class, VampireNighthawk.class, ZetalpaPrimalDawn.class,
        KnightOfGrace.class, KnightOfMalice.class})
class KathrilAspectWarperTest extends BaseCardTest {

    @Test
    void distributesOneCounterForEachKeywordFoundAndBoostsKathril() {
        harness.setGraveyard(player1, List.of(new SerraAngel(), new MireTriton()));
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        chooseCreatureForCounter(otherCreature, CounterType.FLYING);
        chooseCreatureForCounter(otherCreature, CounterType.DEATHTOUCH);
        chooseCreatureForCounter(otherCreature, CounterType.VIGILANCE);

        assertThat(otherCreature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void ignoresUnlistedKeywordsAndOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new SerraAngel()));

        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(kathril.getTotalCounterCount()).isZero();
    }

    @Test
    void putsAllElevenListedKeywordCountersOnKathrilWhenItIsTheOnlyCreature() {
        harness.setGraveyard(player1, List.of(new AkromaAngelOfWrath(), new ZetalpaPrimalDawn(),
                new VampireNighthawk(), new SlipperyBogbonder(), new GoblinDarkDwellers(),
                new NyxWeaver()));

        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        for (CounterType type : List.of(CounterType.FLYING, CounterType.FIRST_STRIKE,
                CounterType.DOUBLE_STRIKE, CounterType.DEATHTOUCH, CounterType.HEXPROOF,
                CounterType.INDESTRUCTIBLE, CounterType.LIFELINK, CounterType.MENACE,
                CounterType.REACH, CounterType.TRAMPLE, CounterType.VIGILANCE)) {
            assertThat(kathril.getCounterCount(type)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, kathril, type.grantedKeyword())).isTrue();
        }
        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(11);
        assertThat(kathril.getTotalCounterCount()).isEqualTo(22);
        assertThat(gqs.hasKeyword(gd, kathril, Keyword.HASTE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void duplicateGraveyardKeywordsProduceOnlyOneCounterOfEachKind() {
        harness.setGraveyard(player1, List.of(new VampireNighthawk(), new VampireNighthawk()));

        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        assertThat(kathril.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(kathril.getTotalCounterCount()).isEqualTo(6);
    }

    @Test
    void canDistributeCountersAmongDifferentCreaturesIncludingKathril() {
        harness.setGraveyard(player1, List.of(new VampireNighthawk()));
        Permanent other = addCreatureReady(player1, new SlipperyBogbonder());
        Permanent opponent = addCreatureReady(player2, new ZetalpaPrimalDawn());
        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(other.getId(), kathril.getId());
        chooseCreatureForCounter(other, CounterType.FLYING);
        chooseCreatureForCounter(kathril, CounterType.DEATHTOUCH);
        chooseCreatureForCounter(other, CounterType.LIFELINK);

        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(kathril.getTotalCounterCount()).isEqualTo(4);
        assertThat(other.getTotalCounterCount()).isEqualTo(2);
        assertThat(opponent.getTotalCounterCount()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canPutAKeywordCounterOnACreatureThatAlreadyHasThatKeyword() {
        harness.setGraveyard(player1, List.of(new SlipperyBogbonder()));
        Permanent other = addCreatureReady(player1, new SlipperyBogbonder());
        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        chooseCreatureForCounter(other, CounterType.HEXPROOF);

        assertThat(other.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void checksTheGraveyardAtResolutionRatherThanWhenKathrilEnters() {
        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.setGraveyard(player1, List.of(new VampireNighthawk()));
        harness.passBothPriorities();

        assertThat(kathril.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void ignoresKeywordsRemovedFromTheGraveyardBeforeResolution() {
        harness.setGraveyard(player1, List.of(new ZetalpaPrimalDawn()));
        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(kathril.getTotalCounterCount()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void distinctHexproofVariantsEachProduceACounterAndIncreaseKathrilsBonus() {
        harness.setGraveyard(player1, List.of(new KnightOfGrace(), new KnightOfMalice(),
                new SlipperyBogbonder()));

        Permanent kathril = harness.enterBattlefieldAndReturn(player1, new KathrilAspectWarper());
        harness.passBothPriorities();

        assertThat(kathril.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.HEXPROOF)).isEqualTo(1);
        assertThat(kathril.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(kathril.getTotalCounterCount()).isEqualTo(8);
    }

    private void chooseCreatureForCounter(Permanent creature, CounterType counterType) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        assertThat(creature.getCounterCount(counterType)).isEqualTo(1);
    }
}
