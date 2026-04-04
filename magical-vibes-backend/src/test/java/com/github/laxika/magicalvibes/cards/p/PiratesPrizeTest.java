package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PiratesPrizeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has draw 2 and treasure creation effects on SPELL slot")
    void hasCorrectSpellEffects() {
        PiratesPrize card = new PiratesPrize();

        assertThat(card.getEffects(EffectSlot.SPELL))
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(DrawCardEffect.class);
                    assertThat(((DrawCardEffect) effects.get(0)).amount()).isEqualTo(2);
                    assertThat(effects.get(1)).isInstanceOf(CreateTokenEffect.class);
                });
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as a sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving draws two cards and creates a Treasure token")
    void resolvingDrawsTwoAndCreatesTreasure() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Drew two cards (hand was 0 after casting the only card, now should be 2)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Treasure created
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure).isNotNull();
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Treasure token has sacrifice-for-mana activated ability")
    void treasureTokenHasManaAbility() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(treasure.getCard().getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new PiratesPrize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Pirate's Prize"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

}
