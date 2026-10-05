package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuantumReduction.class, GrizzlyBears.class, LlanowarElves.class, Island.class})
class QuantumReductionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -5/-0 and loses all abilities")
    void appliesReductionAndRemovesAbilities() {
        Permanent elves = addCreatureReady(player2, new LlanowarElves());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new QuantumReduction());
        aura.setAttachedTo(elves.getId());

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, elves).losesAllAbilities()).isTrue();
    }

    @Test
    @DisplayName("Teamwork lets Quantum Reduction be cast at instant speed")
    void teamworkAllowsInstantSpeedCast() {
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gd.stack.getLast().isTeamworkCostPaid()).isTrue();
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
    }

    @Test
    @DisplayName("Instant-speed casting requires paying teamwork")
    void instantSpeedCastingRequiresTeamwork() {
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, target.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must pay teamwork");
        assertThat(teammate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Teamwork is optional at sorcery speed and the Aura removes mana abilities")
    void castsWithoutTeamworkAndRemovesManaAbility() {
        Permanent target = addCreatureReady(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("lost its abilities");
    }

    @Test
    @DisplayName("Teamwork can combine the power of summoning-sick creatures")
    void teamworkCombinesSummoningSickCreatures() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Teamwork rejects creatures whose combined power is too small")
    void rejectsInsufficientTeamworkPower() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent teammate = addCreatureReady(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("total power at least 2");
        assertThat(teammate.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Teamwork cannot tap an opponent's creature")
    void rejectsOpponentCreatureForTeamwork() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuantumReduction()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creatures you control");
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Negative power of an unchosen creature does not prevent teamwork flash")
    void negativePowerOfUnchosenCreatureDoesNotPreventFlash() {
        Permanent reducedCreature = addCreatureReady(player1, new LlanowarElves());
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new QuantumReduction(), new QuantumReduction()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, reducedCreature.getId());
        assertThat(gqs.getEffectivePower(gd, reducedCreature)).isEqualTo(-4);
        addMana();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castSorceryTappingPermanents(player1, 0, target.getId(), List.of(teammate.getId()));
        harness.passBothPriorities();

        assertThat(teammate.isTapped()).isTrue();
        assertThat(reducedCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
