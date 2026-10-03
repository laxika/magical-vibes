package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChishiroTheShatteredBlade.class, GrizzlyBears.class, HolyStrength.class,
        LeoninScimitar.class, GloriousAnthem.class})
class ChishiroTheShatteredBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Spirit when an Aura or Equipment enters under your control")
    void createsSpiritWhenAuraOrEquipmentEnters() {
        addCreatureReady(player1, new ChishiroTheShatteredBlade());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(spirit.getCard().hasKeyword(Keyword.MENACE)).isTrue();
            assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on each modified creature you control at your end step")
    void putsCountersOnModifiedCreaturesAtEndStep() {
        addCreatureReady(player1, new ChishiroTheShatteredBlade());
        Permanent counterCreature = addCreatureReady(player1, new GrizzlyBears());
        counterCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        enchanted.setAttachedTo(enchantedCreature.getId());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(equippedCreature.getId());
        Permanent unmodifiedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(counterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(equippedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(unmodifiedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enchanted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equipment.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void auraControlMattersForModificationButEquipmentControlDoesNot() {
        Permanent chishiro = addCreatureReady(player1, new ChishiroTheShatteredBlade());
        chishiro.setCounterCount(CounterType.CHARGE, 1);
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(enchantedCreature.getId());
        Permanent equippedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(equippedCreature.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(chishiro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enchantedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equippedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void checksModifiedStatusWhenEndStepAbilityResolves() {
        addCreatureReady(player1, new ChishiroTheShatteredBlade());
        Permanent previouslyModified = addCreatureReady(player1, new GrizzlyBears());
        previouslyModified.setCounterCount(CounterType.CHARGE, 1);
        Permanent newlyModified = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        previouslyModified.setCounterCount(CounterType.CHARGE, 0);
        newlyModified.setCounterCount(CounterType.CHARGE, 1);
        resolveAllTriggers();

        assertThat(previouslyModified.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(newlyModified.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotPutCountersOnCreaturesAtOpponentsEndStep() {
        Permanent chishiro = addCreatureReady(player1, new ChishiroTheShatteredBlade());
        chishiro.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(chishiro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void createsSpiritForControlledAuraEnchantingOpponentsCreature() {
        addCreatureReady(player1, new ChishiroTheShatteredBlade());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void doesNotCreateSpiritForOpponentsAuraOrEquipment() {
        addCreatureReady(player1, new ChishiroTheShatteredBlade());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new HolyStrength(), new LeoninScimitar()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player2, 0, target.getId());
        resolveAllTriggers();
        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void nonAuraEnchantmentNeitherCreatesSpiritNorModifiesCreatures() {
        Permanent chishiro = addCreatureReady(player1, new ChishiroTheShatteredBlade());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(chishiro.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
