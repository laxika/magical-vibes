package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.m.ManaMatrix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ragnar.class, DurkwoodBoars.class, ManaMatrix.class})
class RagnarTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates a target creature and taps Ragnar")
    void regeneratesTargetCreature() {
        Permanent source = addCreatureReady(player1, new Ragnar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without all three colored mana")
    void cannotActivateWithoutAllColoredMana() {
        addCreatureReady(player1, new Ragnar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new Ragnar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ManaMatrix());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canRegenerateItself() {
        Permanent source = addCreatureReady(player1, new Ragnar());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();
        source.setMarkedDamage(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, source));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(source.getRegenerationShield()).isZero();
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(source.isTapped()).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, source));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(source.getCard());
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Ragnar());
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent source = addCreatureReady(player1, new Ragnar());
        source.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterRagnarLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new Ragnar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, source));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void shieldExpiresAtEndOfTurn() {
        addCreatureReady(player1, new Ragnar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getRegenerationShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    void abilityDoesNotRegenerateRagnarWhenTargetLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new Ragnar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DurkwoodBoars());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(source.getRegenerationShield()).isZero();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
