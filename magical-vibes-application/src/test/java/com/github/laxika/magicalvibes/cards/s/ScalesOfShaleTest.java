package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({ScalesOfShale.class, RapidHybridization.class, GrizzlyBears.class, FountainOfYouth.class})
class ScalesOfShaleTest extends BaseCardTest {

    @Test
    void affinityForLizardsReducesCostAndAppliesEffects() {
        createFrogLizard(player1);
        Permanent lizard = findPermanent(player1, "Frog Lizard");

        harness.setHand(player1, List.of(new ScalesOfShale()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, lizard.getId());
        harness.passBothPriorities();

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
        harness.castInstant(player1, 0, lizard.getId());
        harness.passBothPriorities();

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

    private void createFrogLizard(Player tokenController) {
        Permanent bear = harness.addToBattlefieldAndReturn(tokenController, new GrizzlyBears());
        harness.setHand(player1, List.of(new RapidHybridization()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
    }
}
