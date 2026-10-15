package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcceleratedEvolution.class, GrizzlyBears.class, AuraFinesse.class})
class AcceleratedEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the enchanted creature and grants hexproof until end of turn")
    void boostsAndGrantsTemporaryHexproof() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can be cast during an opponent's turn because it has flash")
    void canBeCastDuringOpponentsTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AcceleratedEvolution()));
        addCastingMana(player1);

        harness.passPriority(player2);
        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can target only a creature controlled by the caster")
    void cannotTargetOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AcceleratedEvolution()));
        addCastingMana(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new AcceleratedEvolution()));
        addCastingMana(player1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The boost applies immediately, but hexproof waits for the enter trigger")
    void hexproofWaitsForTriggerResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AcceleratedEvolution()));
        addCastingMana(player1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @CardUsed({AcceleratedEvolution.class, GrizzlyBears.class, AuraFinesse.class})
    @DisplayName("The enter trigger grants hexproof to the creature enchanted when it resolves")
    void movingAuraBeforeTriggerResolvesChangesHexproofRecipient() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new AcceleratedEvolution()));
        addCastingMana(player1);
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0,
                List.of(harness.getPermanentId(player1, "Accelerated Evolution"), destination.getId()));

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, destination)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, destination, Keyword.HEXPROOF)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, original, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, destination, Keyword.HEXPROOF)).isTrue();
    }

    private void addCastingMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
