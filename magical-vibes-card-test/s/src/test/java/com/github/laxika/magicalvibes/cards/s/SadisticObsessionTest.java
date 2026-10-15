package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SadisticObsession.class, GrizzlyBears.class, FountainOfYouth.class})
class SadisticObsessionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature can tap to put a -1/-1 counter on target creature")
    void enchantedCreaturePutsMinusOneMinusOneCounterOnTargetCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantedCreature.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SadisticObsession());
        aura.setAttachedTo(enchantedCreature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(enchantedCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability cannot target a noncreature permanent")
    void grantedAbilityCannotTargetNonCreature() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        enchantedCreature.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SadisticObsession());
        aura.setAttachedTo(enchantedCreature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
    @Test
    void auraCanEnchantOpponentsCreatureAndItsControllerCanActivate() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new SadisticObsession()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sadistic Obsession");
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void summoningSickCreatureCannotActivateGrantedTapAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SadisticObsession());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void tappedCreatureCannotActivateGrantedAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SadisticObsession());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void grantedAbilityRequiresBlackMana() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SadisticObsession());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void activatedAbilityStillResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SadisticObsession());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        creature.untap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
