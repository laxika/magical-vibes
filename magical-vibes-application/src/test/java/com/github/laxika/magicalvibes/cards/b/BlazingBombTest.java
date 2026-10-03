package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlazingBomb.class, GrizzlyBears.class, HillGiant.class, Hurricane.class, Mountain.class, Shock.class})
class BlazingBombTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell with less than four mana spent does not add a counter")
    void cheapNoncreatureSpellDoesNotAddCounter() {
        Permanent bomb = addBomb();
        setUpMainPhase();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(bomb.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A noncreature spell with at least four mana spent adds a counter")
    void fourManaNoncreatureSpellAddsCounter() {
        Permanent bomb = addBomb();
        setUpMainPhase();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(bomb.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature spells do not trigger the counter ability")
    void creatureSpellDoesNotTrigger() {
        Permanent bomb = addBomb();
        setUpMainPhase();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(bomb.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Blow Up sacrifices the bomb and deals damage equal to its power")
    void blowUpSacrificesAndDealsPowerDamage() {
        addReadyBomb();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = bears.getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Blazing Bomb");
        harness.assertInGraveyard(player1, "Blazing Bomb");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blow Up cannot target a noncreature permanent")
    void blowUpCannotTargetNoncreaturePermanent() {
        Permanent bomb = addReadyBomb();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bomb.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bomb);
    }

    @Test
    void fourManaCreatureSpellDoesNotTrigger() {
        Permanent bomb = addBomb();
        setUpMainPhase();
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(bomb.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void threeManaNoncreatureSpellDoesNotTrigger() {
        Permanent bomb = addBomb();
        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(bomb.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsFourManaSpellDoesNotTrigger() {
        Permanent bomb = addBomb();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Hurricane()));
        harness.castAndResolveSorcery(player2, 0, 3);
        assertThat(bomb.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void blowUpUsesCountersOnSacrificedBomb() {
        Permanent bomb = addReadyBomb();
        bomb.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player2, new HillGiant());
        setUpMainPhase();
        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Hill Giant"));
        harness.assertInGraveyard(player1, "Blazing Bomb");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void blowUpCannotBeActivatedDuringUpkeep() {
        Permanent bomb = addReadyBomb();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bomb.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Blazing Bomb");
    }

    @Test
    void blowUpCannotBeActivatedOnOpponentsTurn() {
        Permanent bomb = addReadyBomb();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bomb.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Blazing Bomb");
    }

    @Test
    void summoningSickBombCannotBlowUp() {
        Permanent bomb = addBomb();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setUpMainPhase();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bomb.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Blazing Bomb");
    }

    @Test
    void blowUpCannotTargetAPlayer() {
        Permanent bomb = addReadyBomb();
        setUpMainPhase();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bomb.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Blazing Bomb");
    }

    private Permanent addBomb() {
        return harness.addToBattlefieldAndReturn(player1, new BlazingBomb());
    }

    private Permanent addReadyBomb() {
        return addCreatureReady(player1, new BlazingBomb());
    }

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
