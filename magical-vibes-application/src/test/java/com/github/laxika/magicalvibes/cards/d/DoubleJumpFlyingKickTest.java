package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({DoubleJumpFlyingKick.class, GrizzlyBears.class, HillGiant.class})
class DoubleJumpFlyingKickTest extends BaseCardTest {

    private static final int DOUBLE_JUMP = 0;
    private static final int FLYING_KICK = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Double Jump puts a flying counter on the creature and sets it to 5/5")
    void doubleJumpAddsFlyingCounterAndSetsBaseStats() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addBlueMana();

        harness.castInstant(player1, 0, DOUBLE_JUMP, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(creature.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Double Jump's base power and toughness wear off but its flying counter remains")
    void doubleJumpExpiresAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addBlueMana();

        harness.castInstant(player1, 0, DOUBLE_JUMP, creature.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(creature.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flying Kick deals damage equal to the source creature's power")
    void flyingKickDealsPowerDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addRedMana();

        harness.castModalInstant(player1, 0, FLYING_KICK, List.of(source.getId(), victim.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Fuse resolves Double Jump before Flying Kick")
    void fuseUsesTheBoostedPower() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addFuseMana();

        harness.castModalInstant(player1, 0, FUSE,
                List.of(source.getId(), source.getId(), victim.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(source.getEffectivePower()).isEqualTo(5);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Flying Kick cannot target a creature controlled by the caster")
    void flyingKickRequiresAnOpponentCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addRedMana();

        UUID sourceId = source.getId();
        UUID victimId = victim.getId();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, FLYING_KICK, List.of(sourceId, victimId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fuse can boost a different creature from the one dealing damage")
    void fuseUsesIndependentCreatureTargets() {
        Permanent boosted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addFuseMana();

        harness.castModalInstant(player1, 0, FUSE,
                List.of(boosted.getId(), source.getId(), victim.getId()));
        harness.passBothPriorities();

        assertThat(boosted.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(boosted.getEffectivePower()).isEqualTo(5);
        assertThat(source.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(source.getEffectivePower()).isEqualTo(2);
        assertThat(victim.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Fuse still resolves Double Jump when Flying Kick's victim has left")
    void fuseBoostsCreatureWhenVictimIsGone() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addFuseMana();

        harness.castModalInstant(player1, 0, FUSE,
                List.of(source.getId(), source.getId(), victim.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(source.getEffectivePower()).isEqualTo(5);
        assertThat(source.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Double Jump // Flying Kick");
    }

    @Test
    @DisplayName("Flying Kick does not deal damage if its source leaves before resolution")
    void flyingKickNeedsItsSourceToRemainLegal() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addRedMana();

        harness.castModalInstant(player1, 0, FLYING_KICK, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Double Jump // Flying Kick");
    }

    @Test
    @DisplayName("Double Jump cannot target an opponent's creature")
    void doubleJumpRequiresYourCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoubleJumpFlyingKick()));
        addBlueMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, DOUBLE_JUMP, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlueMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addRedMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addFuseMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
