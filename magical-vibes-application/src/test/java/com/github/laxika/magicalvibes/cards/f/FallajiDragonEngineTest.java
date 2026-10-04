package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallajiDragonEngine.class, ArgothianSprite.class})
class FallajiDragonEngineTest extends BaseCardTest {

    @Test
    void normalCastUsesPrintedCharacteristics() {
        harness.setHand(player1, List.of(new FallajiDragonEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent engine = findPermanent(player1, "Fallaji Dragon Engine");
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, engine)).isEmpty();
    }

    @Test
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new FallajiDragonEngine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent engine = findPermanent(player1, "Fallaji Dragon Engine");
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, engine)).containsExactly(CardColor.RED);
    }

    @Test
    void activatedAbilityBoostsPowerUntilEndOfTurn() {
        Permanent engine = addCreatureReady(player1, new FallajiDragonEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(5);
    }

    @Test
    void repeatedActivationsStackWithoutTappingTheCreature() {
        Permanent engine = addCreatureReady(player1, new FallajiDragonEngine());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(5);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(5);
        assertThat(engine.isTapped()).isFalse();
    }

    @Test
    void prototypeKeepsActivatedAbilityWhileSummoningSickAndBoostExpires() {
        harness.setHand(player1, List.of(new FallajiDragonEngine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent engine = findPermanent(player1, "Fallaji Dragon Engine");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, engine)).containsExactly(CardColor.RED);
    }

    @Test
    void prototypeRetainsFlyingAndCannotBeBlockedByGroundCreature() {
        harness.setHand(player1, List.of(new FallajiDragonEngine()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        findPermanent(player1, "Fallaji Dragon Engine").setSummoningSick(false);
        addCreatureReady(player2, new ArgothianSprite());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
