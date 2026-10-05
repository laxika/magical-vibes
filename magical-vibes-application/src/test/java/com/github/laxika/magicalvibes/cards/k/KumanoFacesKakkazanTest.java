package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.e.EtchingOfKumano;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TamiyosCompleation;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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
        GrizzlyBears.class, LlanowarElves.class, ProdigalPyromancer.class,
        Shock.class, TamiyosCompleation.class, WrathOfGod.class, ActOfTreason.class})
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
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(first).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(second).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III transforms into Etching of Kumano")
    void chapterIIITransformsAndExilesCreaturesDamagedByYourSources() {
        addSagaWithLore(2);
        addCreatureReady(player1, new ProdigalPyromancer());
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

    @Test
    void chapterITriggersWhenSagaEnters() {
        harness.setHand(player1, List.of(new KumanoFacesKakkazan()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(findPermanent(player1, "Kumano Faces Kakkazan")
                .getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterIICreatesASeparateTriggerAboveTheCreatureSpell() {
        addSagaWithLore(1);
        advanceToNextChapter();
        resolveAllTriggers();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void etchingExilesCreaturesKilledByYourInstantSpell() {
        transformSaga();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void etchingLosingAbilitiesStopsItsExileReplacement() {
        Permanent etching = transformSaga();
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new TamiyosCompleation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, etching.getId());
        resolveAllTriggers();

        harness.activateAbility(player1, 1, null, elves.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.exiledCards).noneSatisfy(entry ->
                assertThat(entry.card().getName()).isEqualTo("Llanowar Elves"));
    }

    @Test
    void etchingReplacementAppliesWhenItDiesSimultaneouslyWithADamagedCreature() {
        transformSaga();
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player1, 1, null, bears.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Kumano Faces Kakkazan");
        assertThat(gd.exiledCards).anySatisfy(entry ->
                assertThat(entry.card().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void etchingDoesNotExileCreaturesDamagedOnlyByAnOpponentsSource() {
        transformSaga();
        addCreatureReady(player2, new ProdigalPyromancer());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        harness.activateAbility(player2, 0, null, elves.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.exiledCards).noneSatisfy(entry ->
                assertThat(entry.card().getName()).isEqualTo("Llanowar Elves"));
    }

    @Test
    void stealingADamageSourceDoesNotQualifyItsEarlierDamageUnderOpponentControl() {
        transformSaga();
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.activateAbility(player2, 0, null, bears.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, pyromancer.getId());
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(pyromancer), null, player2.getId());
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneSatisfy(entry ->
                assertThat(entry.card().getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    void chapterIIStacksAcrossSagasAndIgnoresNoncreatureSpells() {
        addSagaWithLore(1);
        addSagaWithLore(1);
        advanceToNextChapter();
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent transformSaga() {
        addSagaWithLore(2);
        advanceToNextChapter();
        resolveAllTriggers();
        return findPermanent(player1, "Etching of Kumano");
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
