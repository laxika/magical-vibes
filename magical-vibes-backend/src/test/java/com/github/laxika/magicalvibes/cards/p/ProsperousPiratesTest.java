package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProsperousPiratesTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has one ON_ENTER_BATTLEFIELD effect that creates two Treasure tokens")
    void hasCorrectEffect() {
        ProsperousPirates card = new ProsperousPirates();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.tokenName()).isEqualTo("Treasure");
        assertThat(effect.amount()).isEqualTo(2);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.TREASURE);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Prosperous Pirates puts it on the battlefield and triggers ETB")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new ProsperousPirates()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature — ETB trigger goes on stack
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Prosperous Pirates"));
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("When Prosperous Pirates enters, two Treasure tokens are created")
    void etbCreatesTwoTreasureTokens() {
        harness.setHand(player1, List.of(new ProsperousPirates()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .toList();
        assertThat(tokens).hasSize(2);
    }

    @Test
    @DisplayName("ETB Treasure tokens are artifacts with Treasure subtype")
    void tokensHaveCorrectProperties() {
        harness.setHand(player1, List.of(new ProsperousPirates()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .findFirst().orElseThrow();

        assertThat(token.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Battlefield has Prosperous Pirates plus two Treasure tokens after ETB resolves")
    void battlefieldHasAllPermanents() {
        harness.setHand(player1, List.of(new ProsperousPirates()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Treasure tokens have sacrifice-for-mana activated ability")
    void treasureTokensHaveManaAbility() {
        harness.setHand(player1, List.of(new ProsperousPirates()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Resolve creature
        harness.passBothPriorities(); // Resolve ETB trigger

        Permanent treasure = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treasure"))
                .findFirst().orElseThrow();

        assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(treasure.getCard().getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
    }
}
