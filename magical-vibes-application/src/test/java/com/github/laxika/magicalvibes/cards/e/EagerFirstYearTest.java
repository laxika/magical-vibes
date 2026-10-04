package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntroductionToProphecy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EagerFirstYear.class, BarkshellBlessing.class, GiantGrowth.class, GrizzlyBears.class,
        IntroductionToProphecy.class})
class EagerFirstYearTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant boosts Eager First-Year until end of turn")
    void castingInstantBoostsEagerFirstYear() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(firstYear.getEffectivePower()).isEqualTo(3);
        assertThat(firstYear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Copying an instant boosts Eager First-Year")
    void copyingInstantBoostsEagerFirstYear() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        for (int i = 0; i < 6 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, firstYear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstYear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Eager First-Year")
    void castingCreatureDoesNotBoostEagerFirstYear() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(firstYear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Magecraft boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(firstYear.getEffectivePower()).isEqualTo(2);
        assertThat(firstYear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a sorcery boosts Eager First-Year before the spell resolves")
    void castingSorceryBoostsBeforeSpellResolves() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        harness.setHand(player1, List.of(new IntroductionToProphecy()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        assertThat(gqs.getEffectivePower(gd, firstYear)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, firstYear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstYear)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each instant cast grants a separate magecraft boost")
    void multipleCastsAccumulateBoosts() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, firstYear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, firstYear)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's instant does not grant a magecraft boost")
    void opponentCastingInstantDoesNotBoost() {
        Permanent firstYear = addCreatureReady(player1, new EagerFirstYear());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, firstYear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, firstYear)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
