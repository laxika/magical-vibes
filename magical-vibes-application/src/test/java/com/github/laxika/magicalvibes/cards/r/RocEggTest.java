package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RocEggTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_DEATH CreateTokenEffect creating a 3/3 white Bird token with flying")
    void hasCorrectEffects() {
        RocEgg card = new RocEgg();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Bird");
        assertThat(effect.power()).isEqualTo(3);
        assertThat(effect.toughness()).isEqualTo(3);
        assertThat(effect.color()).isEqualTo(CardColor.WHITE);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.BIRD);
        assertThat(effect.keywords()).containsExactly(Keyword.FLYING);
        assertThat(effect.additionalTypes()).isEmpty();
    }

    // ===== Death trigger =====

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Roc Egg dies, a 3/3 white Bird token with flying is created")
        void deathTriggerCreatesBirdToken() {
            harness.addToBattlefield(player1, new RocEgg());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Roc Egg dies

            GameData gd = harness.getGameData();

            // Roc Egg should be in the graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Roc Egg"));

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the death trigger
            harness.passBothPriorities();

            // A 3/3 Bird token with flying should be on the battlefield
            List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Bird"))
                    .toList();
            assertThat(tokens).hasSize(1);

            Permanent birdToken = tokens.getFirst();
            assertThat(birdToken.getCard().getPower()).isEqualTo(3);
            assertThat(birdToken.getCard().getToughness()).isEqualTo(3);
            assertThat(birdToken.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(birdToken.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(birdToken.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(birdToken.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(birdToken.getCard().isToken()).isTrue();
        }

        @Test
        @DisplayName("Roc Egg death trigger belongs to its controller")
        void deathTriggerBelongsToController() {
            harness.addToBattlefield(player2, new RocEgg());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Roc Egg dies
            harness.passBothPriorities(); // Resolve death trigger

            GameData gd = harness.getGameData();

            // The Bird token should be on player2's battlefield (the Roc Egg's controller)
            List<Permanent> player2Tokens = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Bird"))
                    .toList();
            assertThat(player2Tokens).hasSize(1);

            // Player1 should have no Bird tokens
            List<Permanent> player1Tokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Bird"))
                    .toList();
            assertThat(player1Tokens).isEmpty();
        }
    }
}
