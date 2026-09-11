package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.EtchingOfKumano;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KumanoFacesKakkazan.class, EtchingOfKumano.class, ChandraNalaar.class,
        GrizzlyBears.class, LlanowarElves.class, ProdigalPyromancer.class})
class KumanoFacesKakkazanTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I damages each opponent and their planeswalkers")
    void chapterIDamagesOpponentsAndTheirPlaneswalkers() {
        Permanent ownPlaneswalker = addPlaneswalker(player1, 3);
        Permanent opposingPlaneswalker = addPlaneswalker(player2, 3);
        addSagaWithLore(0);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(ownPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opposingPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Chapter II empowers only the next creature spell")
    void chapterIIEmpowersNextCreatureSpell() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(first).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(second).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III transforms into Etching of Kumano")
    void chapterIIITransformsAndExilesCreaturesDamagedByYourSources() {
        addSagaWithLore(2);
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent etching = findPermanent(player1, "Etching of Kumano");
        assertThat(etching.isTransformed()).isTrue();

        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.exiledCards).anySatisfy(exiled ->
                assertThat(exiled.card().getName()).isEqualTo("Llanowar Elves"));
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new KumanoFacesKakkazan());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        return planeswalker;
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
