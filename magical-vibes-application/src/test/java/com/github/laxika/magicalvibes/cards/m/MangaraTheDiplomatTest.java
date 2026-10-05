package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.b.BasriKet;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TeferiMasterOfTime;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MangaraTheDiplomat.class, AlpineWatchdog.class, Forest.class, Shock.class, BasriKet.class,
        TeferiMasterOfTime.class})
class MangaraTheDiplomatTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent attacks with two creatures")
    void drawsWhenOpponentAttacksWithTwoCreatures() {
        setUpMangaraAndAttackers();

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when an opponent attacks with one creature")
    void doesNotDrawWhenOpponentAttacksWithOneCreature() {
        setUpMangaraAndAttackers();

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts creatures attacking a planeswalker you control")
    void countsCreaturesAttackingPlaneswalker() {
        harness.addToBattlefield(player1, new MangaraTheDiplomat());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2, new AlpineWatchdog());
        addCreatureReady(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0, 1), Map.of(
                0, planeswalker.getId(),
                1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when an opponent casts their second spell of the turn")
    void drawsWhenOpponentCastsSecondSpell() {
        harness.addToBattlefield(player1, new MangaraTheDiplomat());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when you cast your second spell")
    void doesNotDrawWhenControllerCastsSecondSpell() {
        harness.addToBattlefield(player1, new MangaraTheDiplomat());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts attackers split between you and your planeswalker")
    void countsAttackersSplitBetweenPlayerAndPlaneswalker() {
        setUpMangaraAndAttackers();
        Permanent planeswalker = addPlaneswalker(player1);

        declareAttackers(player2, List.of(0, 1), Map.of(1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws only one card when three creatures attack")
    void drawsOnlyOnceForThreeAttackers() {
        setUpMangaraAndAttackers();
        addCreatureReady(player2, new AlpineWatchdog());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(player2, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when Mangara's controller attacks")
    void doesNotDrawWhenControllerAttacks() {
        harness.addToBattlefield(player1, new MangaraTheDiplomat());
        addCreatureReady(player1, new AlpineWatchdog());
        addCreatureReady(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1, 2));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not draw if phasing removes one of two attackers from combat")
    void rechecksAttackersAfterOnePhasesOut() {
        setUpMangaraAndAttackers();
        Permanent teferi = harness.addToBattlefieldAndReturn(player1, new TeferiMasterOfTime());
        teferi.setCounterCount(CounterType.LOYALTY, 3);
        Permanent attacker = gd.playerBattlefields.get(player2.getId()).getFirst();

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        harness.passPriority(player2);
        harness.activateAbility(player1, 1, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(attacker);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An attacker that dies still counts using its last known attack target")
    void drawsAfterOneAttackerDies() {
        setUpMangaraAndAttackers();
        Permanent attacker = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(player2, List.of(0, 1));
        harness.passPriority(player2);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only the second spell triggers, including during Mangara's controller's turn")
    void secondOpponentSpellTriggersOnControllersTurnButThirdDoesNot() {
        harness.addToBattlefield(player1, new MangaraTheDiplomat());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Mangara gains life from combat damage")
    void gainsLifeFromCombatDamage() {
        addCreatureReady(player1, new MangaraTheDiplomat());

        declareAttackers(player1, List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts the opponent's first spell even if cast before Mangara entered")
    void countsSpellCastBeforeMangaraEntered() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new MangaraTheDiplomat());
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void setUpMangaraAndAttackers() {
        harness.addToBattlefield(player1, new MangaraTheDiplomat());
        addCreatureReady(player2, new AlpineWatchdog());
        addCreatureReady(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BasriKet());
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        return permanent;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, java.util.UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
