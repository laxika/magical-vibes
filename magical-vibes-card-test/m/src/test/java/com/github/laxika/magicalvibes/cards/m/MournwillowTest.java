package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mournwillow.class, Divination.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class MournwillowTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, a creature with power 2 or less cannot block this turn")
    void deliriumPreventsSmallCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With delirium, a creature with power 3 can still block")
    void deliriumDoesNotPreventLargeCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Without delirium, a creature with power 2 can block")
    void withoutDeliriumDoesNotPreventBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Forest()));

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker), 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Delirium also prevents small creatures entering after resolution from blocking")
    void laterCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A small creature raised above power 2 after resolution can block")
    void increasedPowerAllowsBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A large creature reduced to power 2 after resolution cannot block")
    void decreasedPowerPreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Losing delirium after resolution does not remove the blocking restriction")
    void restrictionPersistsAfterLosingDelirium() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GrizzlyBears());
        castMournwillow(List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));
        harness.setGraveyard(player1, List.of());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Haste lets Mournwillow attack on the turn it enters without delirium")
    void canAttackImmediately() {
        castMournwillow(List.of());
        Permanent mournwillow = findPermanent(player1, "Mournwillow");

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mournwillow)));

        assertThat(mournwillow.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Delirium is checked again when the enters ability resolves")
    void losingDeliriumBeforeResolutionAllowsBlocking() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Divination(), new Forest()));
        harness.setHand(player1, List.of(new Mournwillow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock(), new Forest()));
        resolveAllTriggers();
        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Four graveyard cards sharing fewer than four types do not trigger delirium")
    void countsTypesInsteadOfCards() {
        Permanent attacker = addCreatureReady(player1, new HillGiant());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        castMournwillow(List.of(new GrizzlyBears(), new HillGiant(), new Shock(), new Forest()));

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
    private void castMournwillow(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new Mournwillow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
