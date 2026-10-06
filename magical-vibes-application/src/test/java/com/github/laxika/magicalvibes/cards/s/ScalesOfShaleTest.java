package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FlamecacheGecko;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RapidHybridization;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScalesOfShale.class, RapidHybridization.class, GrizzlyBears.class, FountainOfYouth.class, FlamecacheGecko.class})
class ScalesOfShaleTest extends BaseCardTest {

    @Test
    void affinityForLizardsReducesCostAndAppliesEffects() {
        createFrogLizard(player1);
        Permanent lizard = findPermanent(player1, "Frog Lizard");

        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, lizard.getId());

        assertThat(lizard.getEffectivePower()).isEqualTo(5);
        assertThat(lizard.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void effectsWearOffAtCleanup() {
        createFrogLizard(player1);
        Permanent lizard = findPermanent(player1, "Frog Lizard");

        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, lizard.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lizard.getEffectivePower()).isEqualTo(3);
        assertThat(lizard.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, lizard, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void opponentLizardsDoNotReduceCost() {
        createFrogLizard(player2);

        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                findPermanent(player2, "Frog Lizard").getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                findPermanent(player1, "Fountain of Youth").getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    void threeLizardsReduceGenericCostToZero() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlamecacheGecko());
        harness.addToBattlefield(player1, new FlamecacheGecko());
        harness.addToBattlefield(player1, new FlamecacheGecko());
        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void affinityCannotPayTheBlackManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlamecacheGecko());
        harness.addToBattlefield(player1, new FlamecacheGecko());
        harness.addToBattlefield(player1, new FlamecacheGecko());
        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void canTargetAnOpponentsNonLizardAtFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void lifelinkGainsLifeFromBoostedCombatDamage() {
        Permanent target = addCreatureReady(player1, new FlamecacheGecko());
        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void indestructiblePreventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FlamecacheGecko());
        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertOnBattlefield(player1, "Flamecache Gecko");
        harness.assertNotInGraveyard(player1, "Flamecache Gecko");
        assertThat(countPermanents(player1, "Frog Lizard")).isEqualTo(1);
    }

    private void createFrogLizard(Player tokenController) {
        Permanent bear = harness.addToBattlefieldAndReturn(tokenController, new GrizzlyBears());
        harness.setHand(player1, List.of(new RapidHybridization()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());
    }
}
