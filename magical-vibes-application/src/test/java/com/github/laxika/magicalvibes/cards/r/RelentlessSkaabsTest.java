package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelentlessSkaabsTest extends BaseCardTest {

    private void resolveUntilEmpty() {
        for (int i = 0; i < 12 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has exile creature from graveyard additional cost")
    void hasExileCreatureCost() {
        RelentlessSkaabs card = new RelentlessSkaabs();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ExileCardFromGraveyardCost.class);
        ExileCardFromGraveyardCost exileCost = (ExileCardFromGraveyardCost) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(exileCost.requiredType()).isEqualTo(CardType.CREATURE);
        assertThat(exileCost.trackExiledPower()).isFalse();
    }

    // ===== Casting =====

    @Test
    @DisplayName("Can cast by exiling a creature card from graveyard")
    void castExilesCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Relentless Skaabs");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot cast without a creature card in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card to pay the additional cost")
    void cannotExileNonCreatureCard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new RelentlessSkaabs()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    // ===== Undying =====

    @Test
    @DisplayName("Undying returns Relentless Skaabs with a +1/+1 counter when it dies with no counters")
    void undyingReturnsWithCounter() {
        harness.addToBattlefield(player1, new RelentlessSkaabs());
        Permanent skaabs = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, skaabs.getId());
        resolveUntilEmpty();

        Permanent returnedSkaabs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Relentless Skaabs"))
                .findFirst().orElseThrow();
        assertThat(returnedSkaabs.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returnedSkaabs.getEffectivePower()).isEqualTo(5);
        assertThat(returnedSkaabs.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Relentless Skaabs"));
    }

    @Test
    @DisplayName("Undying does not return Relentless Skaabs when it died with a +1/+1 counter")
    void undyingDoesNotReturnWithCounter() {
        Permanent skaabs = harness.addToBattlefieldAndReturn(player1, new RelentlessSkaabs());
        skaabs.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, skaabs.getId());
        resolveUntilEmpty();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Relentless Skaabs"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Relentless Skaabs"));
    }
}
