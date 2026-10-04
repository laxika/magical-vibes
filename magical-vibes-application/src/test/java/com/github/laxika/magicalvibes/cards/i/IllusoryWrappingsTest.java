package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.e.EnigmaDrake;
import com.github.laxika.magicalvibes.cards.h.HyenaPack;
import com.github.laxika.magicalvibes.cards.l.LuxaRiverShrine;
import com.github.laxika.magicalvibes.cards.s.ShedWeakness;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusoryWrappings.class, HyenaPack.class, LuxaRiverShrine.class,
        AvenInitiate.class, EnigmaDrake.class, ShedWeakness.class})
class IllusoryWrappingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Illusory Wrappings attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HyenaPack());

        harness.setHand(player1, List.of(new IllusoryWrappings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Illusory Wrappings")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature has base power and toughness 0/2")
    void enchantedCreatureHasBaseZeroTwo() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HyenaPack());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusoryWrappings());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to base stats when Illusory Wrappings is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HyenaPack());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new IllusoryWrappings());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Illusory Wrappings")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new HyenaPack());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LuxaRiverShrine());
        harness.setHand(player1, List.of(new IllusoryWrappings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Illusory Wrappings affects only the enchanted opposing creature")
    void enchantsOpposingCreatureWithoutAffectingOthers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HyenaPack());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HyenaPack());

        castWrappings(target);

        assertThat(findPermanent(player1, "Illusory Wrappings").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target, other);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Power and toughness boosts apply whether cast before or after the Aura")
    void boostsApplyOnTopOfBaseStats(boolean boostFirst) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HyenaPack());
        if (boostFirst) {
            castShedWeakness(creature);
        }

        castWrappings(creature);
        if (!boostFirst) {
            castShedWeakness(creature);
        }

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counters still modify the creature's new base power and toughness")
    void countersApplyOnTopOfBaseStats() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HyenaPack());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        castWrappings(creature);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);

        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Hyena Pack");
        harness.assertInGraveyard(player1, "Illusory Wrappings");
        harness.assertNotOnBattlefield(player1, "Hyena Pack");
        harness.assertNotOnBattlefield(player1, "Illusory Wrappings");
    }

    @Test
    @DisplayName("Changing base stats does not remove flying")
    void enchantedCreatureRetainsFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AvenInitiate());

        castWrappings(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Illusory Wrappings overrides characteristic-defined power as the graveyard changes")
    void overridesCharacteristicDefinedPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new EnigmaDrake());
        harness.setGraveyard(player2, List.of(new ShedWeakness(), new ShedWeakness()));

        castWrappings(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.setGraveyard(player2, List.of(new ShedWeakness(), new ShedWeakness(), new ShedWeakness()));
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Aura goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HyenaPack());
        harness.setHand(player1, List.of(new IllusoryWrappings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setHand(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Illusory Wrappings");
        harness.assertNotOnBattlefield(player1, "Illusory Wrappings");
    }

    private void castWrappings(Permanent target) {
        harness.setHand(player1, List.of(new IllusoryWrappings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void castShedWeakness(Permanent target) {
        harness.setHand(player1, List.of(new ShedWeakness()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, false);
    }
}
