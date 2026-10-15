package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SporeFrog;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DryadArbor.class, FountainOfYouth.class, Reprobation.class, SerraAngel.class,
        SporeFrog.class, UniversalAutomaton.class})
class ReprobationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Reprobation());
        aura.setAttachedTo(angel.getId());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardTypes(gd, angel)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.COWARD);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Reprobation());
        aura.setAttachedTo(angel.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new Reprobation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void castingRemovesArtifactTypeAndReplacesChangelingCreatureTypes() {
        Permanent automaton = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        harness.setHand(player1, List.of(new Reprobation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, automaton.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Reprobation");
        assertThat(gqs.getEffectiveCardTypes(gd, automaton)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, automaton)).containsExactly(CardSubtype.COWARD);
        assertThat(gqs.getEffectivePower(gd, automaton)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, automaton)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, automaton, Keyword.CHANGELING)).isFalse();
    }

    @Test
    void countersAndModifiersApplyAboveBasePowerAndToughness() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        enchanted.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        enchanted.setPowerModifier(3);
        enchanted.setToughnessModifier(3);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Reprobation());
        aura.setAttachedTo(enchanted.getId());

        assertThat(gqs.getEffectivePower(gd, enchanted)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, enchanted)).isEqualTo(6);
        assertThat(gqs.getEffectiveCardTypes(gd, other)).contains(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.hasKeyword(gd, other, Keyword.CHANGELING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(1);
    }

    @Test
    void enchantedCreatureCannotActivateItsAbility() {
        Permanent frog = harness.addToBattlefieldAndReturn(player1, new SporeFrog());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Reprobation());
        aura.setAttachedTo(frog.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Spore Frog");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingLandTypeAlsoRemovesForestSubtype() {
        Permanent arbor = harness.addToBattlefieldAndReturn(player2, new DryadArbor());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Reprobation());
        aura.setAttachedTo(arbor.getId());

        assertThat(gqs.getEffectiveCardTypes(gd, arbor)).containsExactly(CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, arbor)).containsExactly(CardSubtype.COWARD);
        assertThat(gqs.hasEffectiveSubtype(gd, arbor, CardSubtype.FOREST)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectiveCardTypes(gd, arbor)).contains(CardType.LAND, CardType.CREATURE);
        assertThat(gqs.hasEffectiveSubtype(gd, arbor, CardSubtype.FOREST)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, arbor, CardSubtype.DRYAD)).isTrue();
    }

    @Test
    void targetLeavingBeforeResolutionPreventsAttachment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton());
        harness.setHand(player1, List.of(new Reprobation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Reprobation");
        harness.assertInGraveyard(player1, "Reprobation");
    }
}
