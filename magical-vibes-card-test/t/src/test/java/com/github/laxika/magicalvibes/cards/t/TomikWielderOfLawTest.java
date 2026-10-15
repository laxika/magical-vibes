package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KayaSpiritsJustice;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TomikWielderOfLaw.class, KayaSpiritsJustice.class, NoviceInspector.class, Forest.class, Murder.class})
class TomikWielderOfLawTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for planeswalkers reduces the generic mana cost")
    void affinityForPlaneswalkersReducesGenericCost() {
        addPlaneswalker(player1);
        harness.setHand(player1, List.of(new TomikWielderOfLaw()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity counts only planeswalkers controlled by the spell's controller")
    void affinityCountsOnlyControlledPlaneswalkers() {
        addPlaneswalker(player2);
        harness.setHand(player1, List.of(new TomikWielderOfLaw()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Opponent attacking with two creatures makes Tomik's opponent lose life and Tomik's controller draw")
    void opponentAttackingWithTwoCreaturesTriggersBothEffects() {
        harness.addToBattlefield(player1, new TomikWielderOfLaw());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2);
        addCreatureReady(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0, 1), Map.of(
                0, player1.getId(),
                1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tomik does not trigger for an opponent attacking with one creature")
    void doesNotTriggerForOneAttacker() {
        harness.addToBattlefield(player1, new TomikWielderOfLaw());
        addCreatureReady(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0), null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void affinityDoesNotReduceColoredMana() {
        addPlaneswalker(player1);
        harness.setHand(player1, List.of(new TomikWielderOfLaw()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void nonPlaneswalkerDoesNotReduceCost() {
        harness.addToBattlefield(player1, new NoviceInspector());
        harness.setHand(player1, List.of(new TomikWielderOfLaw()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void twoCreaturesAttackingOnlyPlaneswalkerTriggerOnce() {
        harness.addToBattlefield(player1, new TomikWielderOfLaw());
        Permanent planeswalker = addPlaneswalker(player1);
        addCreatureReady(player2);
        addCreatureReady(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0, 1), Map.of(
                0, planeswalker.getId(), 1, planeswalker.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void controllersOwnAttackDoesNotTrigger() {
        harness.addToBattlefield(player1, new TomikWielderOfLaw());
        addCreatureReady(player1);
        addCreatureReady(player1);

        declareAttackers(player1, List.of(1, 2), null);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingOneOfTwoAttackersStopsLifeLossAndDraw() {
        harness.addToBattlefield(player1, new TomikWielderOfLaw());
        Permanent attacker = addCreatureReady(player2);
        addCreatureReady(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(player2, List.of(0, 1), null);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void removingOneOfThreeAttackersStillAllowsLifeLossAndDraw() {
        harness.addToBattlefield(player1, new TomikWielderOfLaw());
        Permanent attacker = addCreatureReady(player2);
        addCreatureReady(player2);
        addCreatureReady(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(player2, List.of(0, 1, 2), null);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void removingTomikDoesNotStopAlreadyTriggeredAbility() {
        Permanent tomik = harness.addToBattlefieldAndReturn(player1, new TomikWielderOfLaw());
        addCreatureReady(player2);
        addCreatureReady(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        declareAttackers(player2, List.of(0, 1), null);
        harness.castAndResolveInstant(player2, 0, tomik.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tomik);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new KayaSpiritsJustice());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        return planeswalker;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new NoviceInspector());
        creature.setSummoningSick(false);
        return creature;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }
}
