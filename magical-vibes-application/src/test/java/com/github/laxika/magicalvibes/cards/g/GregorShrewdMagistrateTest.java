package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GregorShrewdMagistrate.class, Forest.class, SwordsToPlowshares.class})
class GregorShrewdMagistrateTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to its power after dealing combat damage to a player")
    void drawsCardsEqualToPower() {
        addAttackingGregor();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Uses its current power when the trigger resolves")
    void usesCurrentPowerAtResolution() {
        addAttackingGregor();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        Permanent gregor = findPermanent(player1, "Gregor, Shrewd Magistrate");
        gregor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when legally blocked by a creature with equal power")
    void doesNotTriggerWhenBlocked() {
        addAttackingGregor();
        addCreatureReady(player2, new GregorShrewdMagistrate());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotBeBlockedByGreaterPower() {
        addAttackingGregor();
        Permanent blocker = addCreatureReady(player2, new GregorShrewdMagistrate());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    void canBeBlockedByLowerPower() {
        Permanent gregor = addAttackingGregor();
        gregor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, new GregorShrewdMagistrate());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombatAndTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void usesLastKnownPowerAfterLeavingBattlefield() {
        Permanent gregor = addAttackingGregor();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        gregor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, gregor.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Gregor, Shrewd Magistrate");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttackingGregor() {
        Permanent gregor = addCreatureReady(player1, new GregorShrewdMagistrate());
        gregor.setAttacking(true);
        return gregor;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
