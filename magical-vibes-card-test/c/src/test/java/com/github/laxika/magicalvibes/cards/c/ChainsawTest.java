package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnimateArtifact;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
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

@CardUsed({Chainsaw.class, GrizzlyBears.class, Forest.class, WrathOfGod.class, AnimateArtifact.class})
class ChainsawTest extends BaseCardTest {

    @Test
    @DisplayName("When Chainsaw enters, it deals 3 damage to up to one target creature")
    void entersAndDealsDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Chainsaw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Chainsaw may enter without choosing a creature")
    void mayEnterWithoutTarget() {
        harness.setHand(player1, List.of(new Chainsaw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chainsaw");
    }

    @Test
    @DisplayName("Chainsaw cannot target a land with its entering ability")
    void enteringAbilityCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Chainsaw()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Chainsaw gets a rev counter whenever a creature dies")
    void getsRevCounterWhenCreatureDies() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        putIntoGraveyard(victim);

        assertThat(chainsaw.getCounterCount(CounterType.REV)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chainsaw gets only one rev counter when multiple creatures die simultaneously")
    void getsOneRevCounterForSimultaneousCreatureDeaths() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(chainsaw.getCounterCount(CounterType.REV)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each rev counter gives the equipped creature +1/+0")
    void revCountersBoostEquippedCreature() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        chainsaw.setAttachedTo(creature.getId());
        chainsaw.setCounterCount(CounterType.REV, 2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {3} attaches Chainsaw to a creature you control")
    void equipsCreature() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(chainsaw.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Separate creature deaths in the same turn each add a rev counter")
    void separateDeathsEachAddRevCounter() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        putIntoGraveyard(ownCreature);
        putIntoGraveyard(opposingCreature);

        assertThat(chainsaw.getCounterCount(CounterType.REV)).isEqualTo(2);
    }

    @Test
    @DisplayName("A noncreature dying does not add a rev counter")
    void noncreatureDeathDoesNotAddRevCounter() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        putIntoGraveyard(land);

        assertThat(chainsaw.getCounterCount(CounterType.REV)).isZero();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpposingCreature() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(chainsaw.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An animated Chainsaw triggers when it is the only creature that dies")
    void animatedChainsawSeesItsOwnDeath() {
        Permanent chainsaw = harness.addToBattlefieldAndReturn(player1, new Chainsaw());
        harness.setHand(player1, List.of(new AnimateArtifact(), new WrathOfGod()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, chainsaw.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, chainsaw)).isTrue();

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Chainsaw");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(chainsaw.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    private void putIntoGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
