package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
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
}
