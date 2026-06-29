package com.github.laxika.magicalvibes.cards.d;

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

class DoomedTravelerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_DEATH CreateTokenEffect creating a 1/1 white Spirit token with flying")
    void hasCorrectEffects() {
        DoomedTraveler card = new DoomedTraveler();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.tokenName()).isEqualTo("Spirit");
        assertThat(effect.power()).isEqualTo(1);
        assertThat(effect.toughness()).isEqualTo(1);
        assertThat(effect.color()).isEqualTo(CardColor.WHITE);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(effect.keywords()).containsExactly(Keyword.FLYING);
        assertThat(effect.additionalTypes()).isEmpty();
    }

    // ===== Death trigger =====

    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Doomed Traveler dies, a 1/1 white Spirit token with flying is created")
        void deathTriggerCreatesSpiritToken() {
            harness.addToBattlefield(player1, new DoomedTraveler());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Doomed Traveler dies

            GameData gd = harness.getGameData();

            // Doomed Traveler should be in the graveyard
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Doomed Traveler"));

            // Death trigger should be on the stack
            assertThat(gd.stack).hasSize(1);

            // Resolve the death trigger
            harness.passBothPriorities();

            // A 1/1 Spirit token with flying should be on the battlefield
            List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Spirit"))
                    .toList();
            assertThat(tokens).hasSize(1);

            Permanent spiritToken = tokens.getFirst();
            assertThat(spiritToken.getCard().getPower()).isEqualTo(1);
            assertThat(spiritToken.getCard().getToughness()).isEqualTo(1);
            assertThat(spiritToken.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spiritToken.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spiritToken.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(spiritToken.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(spiritToken.getCard().isToken()).isTrue();
        }

        @Test
        @DisplayName("Doomed Traveler death trigger belongs to its controller")
        void deathTriggerBelongsToController() {
            harness.addToBattlefield(player2, new DoomedTraveler());

            harness.setHand(player1, List.of(new WrathOfGod()));
            harness.addMana(player1, ManaColor.WHITE, 4);

            harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
            harness.passBothPriorities(); // Resolve Wrath — Doomed Traveler dies
            harness.passBothPriorities(); // Resolve death trigger

            GameData gd = harness.getGameData();

            // The Spirit token should be on player2's battlefield (the Doomed Traveler's controller)
            List<Permanent> player2Tokens = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Spirit"))
                    .toList();
            assertThat(player2Tokens).hasSize(1);

            // Player1 should have no Spirit tokens
            List<Permanent> player1Tokens = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Spirit"))
                    .toList();
            assertThat(player1Tokens).isEmpty();
        }
    }
}
