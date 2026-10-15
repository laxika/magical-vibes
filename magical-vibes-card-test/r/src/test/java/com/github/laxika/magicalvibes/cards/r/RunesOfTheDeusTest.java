package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CrimsonWisps;
import com.github.laxika.magicalvibes.cards.l.LoamdraggerGiant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunesOfTheDeus.class, HillGiant.class, GrizzlyBears.class,
        HonorGuard.class, FountainOfYouth.class, LoamdraggerGiant.class, CrimsonWisps.class})
class RunesOfTheDeusTest extends BaseCardTest {

    private Permanent attach(Permanent creature) {
        Permanent runes = harness.addToBattlefieldAndReturn(player1, new RunesOfTheDeus());
        runes.setAttachedTo(creature.getId());
        return runes;
    }

    @Test
    @DisplayName("Red enchanted creature gets +1/+1 and double strike, no trample")
    void redCreatureGetsDoubleStrike() {
        Permanent red = addCreatureReady(player1, new HillGiant());
        attach(red);

        // Hill Giant is 3/3, with +1/+1 should be 4/4
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, red, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, red, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Green enchanted creature gets +1/+1 and trample, no double strike")
    void greenCreatureGetsTrample() {
        Permanent green = addCreatureReady(player1, new GrizzlyBears());
        attach(green);

        // Grizzly Bears is 2/2, with +1/+1 should be 3/3
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, green, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, green, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Non-red, non-green enchanted creature gets no boost or keywords")
    void otherColorCreatureUnaffected() {
        Permanent white = addCreatureReady(player1, new HonorGuard());
        attach(white);

        // Honor Guard is a 1/1 white creature — unaffected
        assertThat(gqs.getEffectivePower(gd, white)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, white)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, white, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, white, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Boost and keyword wear off when Runes of the Deus is removed")
    void boostRemovedWhenAuraLeaves() {
        Permanent red = addCreatureReady(player1, new HillGiant());
        Permanent runes = attach(red);

        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, red, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(runes);

        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, red, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Resolving Runes of the Deus attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent red = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new RunesOfTheDeus()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, red.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Runes of the Deus")
                        && p.isAttached()
                        && p.getAttachedTo().equals(red.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Runes of the Deus")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new RunesOfTheDeus()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A red and green creature receives both bonuses and both keywords")
    void multicoloredCreatureGetsBothBonuses() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LoamdraggerGiant());
        Permanent unrelated = harness.addToBattlefieldAndReturn(player1, new LoamdraggerGiant());
        attach(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, unrelated)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, unrelated)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, unrelated, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, unrelated, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Conditional bonuses track a temporary color replacement and its expiration")
    void bonusesTrackColorChanges() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LoamdraggerGiant());
        attach(creature);
        harness.setHand(player1, List.of(new CrimsonWisps()));
        harness.setLibrary(player1, List.of(new LoamdraggerGiant()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }
}
