package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RelentlessAssault;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EstinienVarlineau.class, GrizzlyBears.class, ShivanDragon.class, Shock.class,
        RelentlessAssault.class, UniversalAutomaton.class, Ephemerate.class})
class EstinienVarlineauTest extends BaseCardTest {

    @Test
    void noncreatureSpellAddsCounterAndFlyingUntilEndOfTurn() {
        Permanent estinien = addCreatureReady(player1, new EstinienVarlineau());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(estinien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(estinien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, estinien, Keyword.FLYING)).isTrue();
    }

    @Test
    void drawsAndLosesForEstinienAndDragonCombatDamage() {
        addCreatureReady(player1, new EstinienVarlineau());
        addCreatureReady(player1, new ShivanDragon());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        GrizzlyBears secondDrawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard, secondDrawnCard));
        harness.setLife(player1, 20);

        declareAttackers(List.of(0, 1, 2));
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void nonDragonCombatDamageDoesNotCount() {
        addCreatureReady(player1, new EstinienVarlineau());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void castTriggerResolvesBeforeTheSpellAndFlyingExpires() {
        Permanent estinien = addCreatureReady(player1, new EstinienVarlineau());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(estinien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, estinien, Keyword.FLYING)).isTrue();
        harness.assertLife(player2, 20);

        resolveAllTriggers();
        harness.assertLife(player2, 18);
        harness.forceStep(TurnStep.CLEANUP);
        gs.advanceStep(gd);

        assertThat(gqs.hasKeyword(gd, estinien, Keyword.FLYING)).isFalse();
        assertThat(estinien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTrigger() {
        Permanent estinien = addCreatureReady(player1, new EstinienVarlineau());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(estinien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, estinien, Keyword.FLYING)).isFalse();
    }

    @Test
    void artifactCreatureSpellDoesNotTrigger() {
        Permanent estinien = addCreatureReady(player1, new EstinienVarlineau());

        harness.castFromHand(player1, new UniversalAutomaton(), "{1}");
        resolveAllTriggers();

        assertThat(estinien.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, estinien, Keyword.FLYING)).isFalse();
    }

    @Test
    void dragonCombatDamageCountsWithoutEstinienAttacking() {
        addCreatureReady(player1, new EstinienVarlineau());
        addCreatureReady(player1, new ShivanDragon());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 19);
    }

    @Test
    void noncombatDamageDoesNotCount() {
        addCreatureReady(player1, new EstinienVarlineau());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        declareAttackers(List.of());
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void changelingCombatDamageCountsAsDragonDamage() {
        addCreatureReady(player1, new EstinienVarlineau());
        addCreatureReady(player1, new UniversalAutomaton());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 19);
    }

    @Test
    void blinkedEstinienDoesNotCountThePreviousObjectsDamage() {
        Permanent estinien = addCreatureReady(player1, new EstinienVarlineau());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(List.of(0));
            resolveCombat();
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });
        harness.castInstant(player1, 0, estinien.getId());
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Estinien Varlineau").getId()).isNotEqualTo(estinien.getId());

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void dragonDamageStillCountsAfterTheDragonLeavesTheBattlefield() {
        addCreatureReady(player1, new EstinienVarlineau());
        Permanent dragon = addCreatureReady(player1, new ShivanDragon());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Ephemerate()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            declareAttackers(List.of(1));
            resolveCombat();
            harness.passUntil(player1, TurnStep.END_OF_COMBAT);
        });
        harness.castInstant(player1, 0, dragon.getId());
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Shivan Dragon").getId()).isNotEqualTo(dragon.getId());

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, 19);
    }

    @Test
    void additionalMainPhaseDoesNotTriggerAgain() {
        addCreatureReady(player1, new EstinienVarlineau());
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        harness.assertLife(player1, 19);

        harness.castFromHand(player1, new RelentlessAssault(), "{2}{R}{R}");
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        declareAttackers(List.of());
        resolveCombat();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 19);
    }
}
