package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerOwnCreatureDeathsThisTurnEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FreshMeatTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Fresh Meat has correct effect")
    void hasCorrectEffect() {
        FreshMeat card = new FreshMeat();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CreateTokensPerOwnCreatureDeathsThisTurnEffect.class);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Creates one 3/3 Beast token per creature that died this turn")
    void createsTokensPerCreatureDeath() {
        // Simulate 2 creature deaths for player 1 this turn
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 2, Integer::sum);

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        // Should have 2 Beast tokens
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(2);
        for (Permanent beast : beasts) {
            assertThat(beast.getCard().isToken()).isTrue();
            assertThat(beast.getCard().getPower()).isEqualTo(3);
            assertThat(beast.getCard().getToughness()).isEqualTo(3);
            assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        }
    }

    @Test
    @DisplayName("Creates no tokens if no creatures died this turn")
    void createsNoTokensIfNoDeaths() {
        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).isEmpty();
    }

    @Test
    @DisplayName("Only counts controller's creature deaths, not opponent's")
    void onlyCountsControllerDeaths() {
        // Opponent had 3 creatures die, controller had 1
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 3, Integer::sum);
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(1);
    }

    @Test
    @DisplayName("Works after an actual creature death from state-based actions")
    void worksAfterActualCreatureDeath() {
        // Put a creature on the battlefield and kill it
        Permanent elves = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(elves);

        // Simulate the creature dying (put it in graveyard + track the death)
        gd.playerBattlefields.get(player1.getId()).remove(elves);
        gd.playerGraveyards.get(player1.getId()).add(elves.getCard());
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(1);
    }

    @Test
    @DisplayName("Creates tokens for multiple deaths in same turn")
    void createsTokensForMultipleDeaths() {
        // Simulate 5 creatures dying (e.g. board wipe)
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 5, Integer::sum);

        harness.setHand(player1, List.of(new FreshMeat()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> beasts = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(5);
    }
}
