package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaitoShizuki.class, GrizzlyBears.class, PhantomWarrior.class, DrossCrocodile.class})
class KaitoShizukiTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out at your end step when Kaito entered this turn")
    void phasesOutWhenEnteredThisTurn() {
        Permanent kaito = harness.enterBattlefieldAndReturn(player1, new KaitoShizuki());
        kaito.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(kaito);
    }

    @Test
    @DisplayName("Does not phase out at your end step when Kaito entered earlier")
    void doesNotPhaseOutWhenEnteredEarlier() {
        Permanent kaito = addReadyKaito(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kaito);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(kaito);
    }

    @Test
    @DisplayName("+1 draws and discards when you did not attack")
    void plusOneDrawsAndDiscardsWithoutAttack() {
        addReadyKaito(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new PhantomWarrior()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Phantom Warrior");
    }

    @Test
    @DisplayName("+1 skips the discard when you attacked")
    void plusOneSkipsDiscardAfterAttack() {
        addReadyKaito(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new PhantomWarrior()));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Phantom Warrior");
    }

    @Test
    @DisplayName("-2 creates a 1/1 blue Ninja that can't be blocked")
    void minusTwoCreatesUnblockableNinja() {
        addReadyKaito(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent ninja = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(ninja.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(ninja.getCard().getSubtypes()).contains(CardSubtype.NINJA);
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, ninja)).isTrue();
    }

    @Test
    @DisplayName("-7 emblem searches for a blue or black creature after combat damage")
    void minusSevenEmblemSearchesAfterCombatDamage() {
        Permanent kaito = addReadyKaito(7);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new PhantomWarrior()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kaito);

        attacker.setAttacking(true);
        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Phantom Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Kaito phases in only at his controller's untap step and retains loyalty")
    void phasesInAtControllersUntapWithLoyaltyIntact() {
        Permanent kaito = harness.enterBattlefieldAndReturn(player1, new KaitoShizuki());
        kaito.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(kaito);
        harness.performUntapStep(player2);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(kaito);
        harness.performUntapStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kaito);
        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.phasedOutPermanents.getOrDefault(player1.getId(), List.of())).doesNotContain(kaito);
    }

    @Test
    @DisplayName("Kaito does not phase out at an opponent's end step")
    void doesNotPhaseOutAtOpponentsEndStep() {
        Permanent kaito = harness.enterBattlefieldAndReturn(player1, new KaitoShizuki());
        kaito.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kaito);
    }

    @Test
    @DisplayName("+1 with an empty hand discards the card just drawn when you did not attack")
    void plusOneDiscardsDrawnCardFromEmptyHand() {
        Permanent kaito = addReadyKaito(3);
        PhantomWarrior drawn = new PhantomWarrior();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("The emblem can find a black creature and puts it onto the battlefield untapped")
    void emblemFindsBlackCreature() {
        addReadyKaito(7);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        DrossCrocodile found = new DrossCrocodile();
        harness.setLibrary(player1, List.of(found));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent crocodile = findPermanent(player1, "Dross Crocodile");
        assertThat(crocodile.getCard()).isSameAs(found);
        assertThat(crocodile.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(found);
    }

    @Test
    @DisplayName("Each creature dealing combat damage produces a separate emblem search")
    void emblemTriggersForEachCreature() {
        addReadyKaito(7);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        PhantomWarrior blue = new PhantomWarrior();
        DrossCrocodile black = new DrossCrocodile();
        harness.setLibrary(player1, List.of(blue, black));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        first.setAttacking(true);
        second.setAttacking(true);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Phantom Warrior")).hasSize(1);
        assertThat(findPermanents(player1, "Dross Crocodile")).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The emblem cannot find a green creature or a blue noncreature")
    void emblemExcludesWrongColorAndNoncreatures() {
        addReadyKaito(7);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears green = new GrizzlyBears();
        KaitoShizuki noncreature = new KaitoShizuki();
        harness.setLibrary(player1, List.of(green, noncreature));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(green, noncreature);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(findPermanents(player1, "Kaito Shizuki")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The emblem permits failing to find even with an eligible creature in the library")
    void emblemCanFailToFind() {
        addReadyKaito(7);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        PhantomWarrior eligible = new PhantomWarrior();
        harness.setLibrary(player1, List.of(eligible));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
        assertThat(findPermanents(player1, "Phantom Warrior")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Phasing in on a later turn does not cause Kaito to phase out again")
    void doesNotPhaseOutAgainAfterPhasingIn() {
        Permanent kaito = harness.enterBattlefieldAndReturn(player1, new KaitoShizuki());
        kaito.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new PhantomWarrior(), new PhantomWarrior()));
        harness.setLibrary(player2, List.of(new PhantomWarrior(), new PhantomWarrior()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(kaito);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kaito);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kaito);
        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's creature dealing combat damage does not trigger your emblem")
    void emblemDoesNotTriggerForOpponentsCreature() {
        addReadyKaito(7);
        harness.setLibrary(player1, List.of(new PhantomWarrior()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        attacker.setAttacking(true);

        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Phantom Warrior")).isEmpty();
        harness.assertLife(player1, 18);
    }
    private Permanent addReadyKaito(int loyalty) {
        Permanent kaito = harness.addToBattlefieldAndReturn(player1, new KaitoShizuki());
        kaito.setCounterCount(CounterType.LOYALTY, loyalty);
        kaito.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaito;
    }
}
