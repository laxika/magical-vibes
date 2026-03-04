package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SlagstormTest extends BaseCardTest {

    @Test
    @DisplayName("Slagstorm has a ChooseOneEffect with two options")
    void hasCorrectEffects() {
        Slagstorm card = new Slagstorm();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(ChooseOneEffect.class);
        ChooseOneEffect effect = (ChooseOneEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.options()).hasSize(2);
    }

    @Test
    @DisplayName("Casting Slagstorm puts it on the stack as a sorcery")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new Slagstorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Slagstorm");
    }

    @Nested
    @DisplayName("Creature damage mode")
    class CreatureDamageMode {

        @Test
        @DisplayName("Choosing creature mode deals 3 damage to all creatures")
        void deals3DamageToAllCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2
            harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
            harness.setHand(player1, List.of(new Slagstorm()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 0); // mode 0 = creature damage
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            // Players should NOT take damage in creature mode
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Creature mode does not kill creatures with toughness greater than 3")
        void doesNotKillToughCreatures() {
            harness.addToBattlefield(player2, new GiantSpider()); // 2/4
            harness.setHand(player1, List.of(new Slagstorm()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castSorcery(player1, 0, 0); // mode 0 = creature damage
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
        }
    }

    @Nested
    @DisplayName("Player damage mode")
    class PlayerDamageMode {

        @Test
        @DisplayName("Choosing player mode deals 3 damage to each player")
        void deals3DamageToEachPlayer() {
            harness.setHand(player1, List.of(new Slagstorm()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 1); // mode 1 = player damage
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Player mode does not deal damage to creatures")
        void doesNotDealDamageToCreatures() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Slagstorm()));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            harness.castSorcery(player1, 0, 1); // mode 1 = player damage
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            // Creatures should survive
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            // Players take 3 damage
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }
    }

    @Test
    @DisplayName("Choosing invalid mode is rejected at cast time")
    void invalidModeIsRejected() {
        harness.setHand(player1, List.of(new Slagstorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 99))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode index");
    }

    @Test
    @DisplayName("Slagstorm goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Slagstorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0); // mode 0 = creature damage
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Slagstorm"));
    }
}
