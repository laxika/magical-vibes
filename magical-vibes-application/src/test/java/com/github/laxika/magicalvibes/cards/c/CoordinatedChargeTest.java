package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoordinatedCharge.class, GrizzlyBears.class})
class CoordinatedChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all creatures controlled by the caster")
    void boostsOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> {
                    if (permanent.getCard().hasType(CardType.CREATURE)) {
                        assertThat(permanent.getPowerModifier()).isEqualTo(2);
                        assertThat(permanent.getToughnessModifier()).isEqualTo(1);
                    }
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allSatisfy(permanent -> {
                    if (permanent.getCard().hasType(CardType.CREATURE)) {
                        assertThat(permanent.getPowerModifier()).isZero();
                        assertThat(permanent.getToughnessModifier()).isZero();
                    }
                });
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new CoordinatedCharge()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coordinated Charge");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
