package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GutShotTest extends BaseCardTest {

    @Test
    @DisplayName("Gut Shot has correct card properties")
    void hasCorrectProperties() {
        GutShot card = new GutShot();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect effect = (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gut Shot deals 1 damage to target player when paid with red mana")
    void deals1DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Gut Shot deals 1 damage to target creature, destroying a 1/1")
    void deals1DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Can be cast by paying Phyrexian mana with 2 life instead of red mana")
    void canPayPhyrexianWithLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GutShot()));
        // No mana added — entire cost {R/P} paid with 2 life

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 paid 2 life for Phyrexian mana
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Player2 took 1 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Gut Shot goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gut Shot"));
    }
}
