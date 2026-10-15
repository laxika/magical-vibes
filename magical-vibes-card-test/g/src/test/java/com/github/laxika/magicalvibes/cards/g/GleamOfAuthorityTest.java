package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GleamOfAuthority.class, GrizzlyBears.class, FountainOfYouth.class,
        HillGiant.class, MarchOfTheMachines.class})
class GleamOfAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 for each counter on other creatures and vigilance")
    void boostsForCountersOnOtherCreatures() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        enchantedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent firstOther = addCreatureReady(player1, new GrizzlyBears());
        firstOther.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent secondOther = addCreatureReady(player1, new GrizzlyBears());
        secondOther.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        addAura(enchantedCreature);

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, firstOther)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, firstOther, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can tap to bolster the least-tough creature")
    void enchantedCreatureCanBolster() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new HillGiant());
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAura(enchantedCreature);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gleam of Authority cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new GleamOfAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Counters on artifacts made into creatures count toward the boost")
    void countsCountersOnContinuouslyAnimatedArtifacts() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        artifact.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        addAura(enchantedCreature);

        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("The Aura counts its controller's creatures but bolster uses the enchanted creature's controller")
    void opponentControlsGrantedBolsterAbility() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        ownCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentsOtherCreature = addCreatureReady(player2, new GrizzlyBears());
        addAura(enchantedCreature);

        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(4);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(opponentsOtherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bolster allows choosing among tied creatures and the boost updates immediately")
    void bolsterChoosesAmongTiedCreatures() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        addAura(enchantedCreature);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(otherCreature.getId()));

        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(3);
        otherCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting the Aura attaches it and vigilance keeps the attacker untapped")
    void auraResolvesAndGrantsVigilanceInCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GleamOfAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Gleam of Authority").getAttachedTo()).isEqualTo(creature.getId());
        declareAttackers(List.of(0));

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addAura(Permanent enchantedCreature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GleamOfAuthority());
        aura.setAttachedTo(enchantedCreature.getId());
        return aura;
    }
}
