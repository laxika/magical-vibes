package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({BlazingBomb.class, GrizzlyBears.class, Hurricane.class, Mountain.class, Shock.class})
class BlazingBombTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell with less than four mana spent does not add a counter")
    void cheapNoncreatureSpellDoesNotAddCounter() {
        Permanent bomb = addBomb();
        setUpMainPhase();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(bomb.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A noncreature spell with at least four mana spent adds a counter")
    void fourManaNoncreatureSpellAddsCounter() {
        Permanent bomb = addBomb();
        setUpMainPhase();

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

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
        Permanent bomb = addReadyBomb();
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

    private Permanent addBomb() {
        return harness.addToBattlefieldAndReturn(player1, new BlazingBomb());
    }

    private Permanent addReadyBomb() {
        Permanent bomb = addBomb();
        bomb.setSummoningSick(false);
        return bomb;
    }

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
