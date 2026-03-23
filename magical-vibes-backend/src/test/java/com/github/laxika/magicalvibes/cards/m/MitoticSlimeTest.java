package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MitoticSlimeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_DEATH CreateTokenEffect creating two 2/2 Ooze tokens with their own death triggers")
    void hasCorrectEffects() {
        MitoticSlime card = new MitoticSlime();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.amount()).isEqualTo(2);
        assertThat(effect.tokenName()).isEqualTo("Ooze");
        assertThat(effect.power()).isEqualTo(2);
        assertThat(effect.toughness()).isEqualTo(2);
        assertThat(effect.color()).isEqualTo(CardColor.GREEN);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.OOZE);
        assertThat(effect.keywords()).isEmpty();
        assertThat(effect.additionalTypes()).isEmpty();

        // The 2/2 tokens should have their own death trigger
        assertThat(effect.tokenEffects()).hasSize(1);
        assertThat(effect.tokenEffects()).containsKey(EffectSlot.ON_DEATH);
        CardEffect tokenDeathEffect = effect.tokenEffects().get(EffectSlot.ON_DEATH);
        assertThat(tokenDeathEffect).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect innerEffect = (CreateTokenEffect) tokenDeathEffect;
        assertThat(innerEffect.amount()).isEqualTo(2);
        assertThat(innerEffect.tokenName()).isEqualTo("Ooze");
        assertThat(innerEffect.power()).isEqualTo(1);
        assertThat(innerEffect.toughness()).isEqualTo(1);
        assertThat(innerEffect.color()).isEqualTo(CardColor.GREEN);
        assertThat(innerEffect.subtypes()).containsExactly(CardSubtype.OOZE);
    }

    // ===== Death trigger =====

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Mitotic Slime dies, two 2/2 green Ooze tokens are created")
        void deathTriggerCreatesTwoTokens() {
            harness.addToBattlefield(player1, new MitoticSlime());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Slime dies

            GameData gd = harness.getGameData();

            // Mitotic Slime should be in the graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Mitotic Slime"));

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the death trigger
            harness.passBothPriorities();

            // Two 2/2 Ooze tokens should be on the battlefield
            List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze"))
                    .toList();
            assertThat(tokens).hasSize(2);

            for (Permanent token : tokens) {
                assertThat(token.getCard().getPower()).isEqualTo(2);
                assertThat(token.getCard().getToughness()).isEqualTo(2);
                assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                assertThat(token.getCard().getSubtypes()).contains(CardSubtype.OOZE);
                assertThat(token.getCard().isToken()).isTrue();
            }
        }

        @Test
        @DisplayName("2/2 Ooze tokens have their own death trigger that creates 1/1 Ooze tokens")
        void oozeTokensHaveDeathTrigger() {
            harness.addToBattlefield(player1, new MitoticSlime());

            // Kill Mitotic Slime with Wrath of God
            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Slime dies
            harness.passBothPriorities(); // Resolve Slime death trigger — two 2/2 Ooze tokens enter

            GameData gd = harness.getGameData();

            // Verify the 2/2 tokens have ON_DEATH effects
            List<Permanent> oozeTokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze") && p.getCard().getPower() == 2)
                    .toList();
            assertThat(oozeTokens).hasSize(2);

            for (Permanent oozeToken : oozeTokens) {
                assertThat(oozeToken.getCard().getEffects(EffectSlot.ON_DEATH)).hasSize(1);
                assertThat(oozeToken.getCard().getEffects(EffectSlot.ON_DEATH).getFirst())
                        .isInstanceOf(CreateTokenEffect.class);
            }
        }

        @Test
        @DisplayName("Killing a 2/2 Ooze token creates two 1/1 Ooze tokens")
        void killingOozeTokenCreatesSmallTokens() {
            harness.addToBattlefield(player1, new MitoticSlime());

            // Kill Mitotic Slime with Wrath of God
            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Slime dies
            harness.passBothPriorities(); // Resolve Slime death trigger — two 2/2 Ooze tokens enter

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze") && p.getCard().getPower() == 2)
                    .count()).isEqualTo(2);

            // Now kill all creatures again with another Wrath
            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — both 2/2 Ooze tokens die

            // Two death triggers should be on the stack (one per 2/2 token)
            assertThat(gd.stack).hasSize(2);

            // Resolve both death triggers
            harness.passBothPriorities();
            harness.passBothPriorities();

            // Four 1/1 Ooze tokens should now be on the battlefield
            List<Permanent> smallTokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Ooze") && p.getCard().getPower() == 1)
                    .toList();
            assertThat(smallTokens).hasSize(4);

            for (Permanent smallToken : smallTokens) {
                assertThat(smallToken.getCard().getPower()).isEqualTo(1);
                assertThat(smallToken.getCard().getToughness()).isEqualTo(1);
                assertThat(smallToken.getCard().getColor()).isEqualTo(CardColor.GREEN);
                assertThat(smallToken.getCard().getSubtypes()).contains(CardSubtype.OOZE);
                assertThat(smallToken.getCard().isToken()).isTrue();
            }
        }
    }
}
