package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LingeringSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Has spell token creation effect and flashback cost")
    void hasCorrectEffectsAndFlashbackCost() {
        LingeringSouls card = new LingeringSouls();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CreateTokenEffect.class);

        CreateTokenEffect effect = (CreateTokenEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.amount()).isEqualTo(new Fixed(2));
        assertThat(effect.tokenName()).isEqualTo("Spirit");
        assertThat(effect.power()).isEqualTo(1);
        assertThat(effect.toughness()).isEqualTo(1);
        assertThat(effect.color()).isEqualTo(CardColor.WHITE);
        assertThat(effect.subtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(effect.keywords()).containsExactly(Keyword.FLYING);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{1}{B}");
    }

    @Test
    @DisplayName("Casting Lingering Souls creates two 1/1 white Spirit tokens with flying")
    void createsTwoSpiritTokens() {
        harness.setHand(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> spirits = spiritTokens();
        assertThat(spirits).hasSize(2);

        for (Permanent spirit : spirits) {
            assertThat(spirit.getCard().getPower()).isEqualTo(1);
            assertThat(spirit.getCard().getToughness()).isEqualTo(1);
            assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spirit.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(spirit.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(spirit.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("Normal cast puts Lingering Souls into graveyard after resolving")
    void normalCastGoesToGraveyard() {
        harness.setHand(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lingering Souls"));
    }

    @Test
    @DisplayName("Flashback creates two Spirit tokens")
    void flashbackCreatesTwoSpiritTokens() {
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(spiritTokens()).hasSize(2);
    }

    @Test
    @DisplayName("Flashback exiles Lingering Souls after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Lingering Souls"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lingering Souls"));
    }

    @Test
    @DisplayName("Flashback puts Lingering Souls on the stack as a sorcery")
    void flashbackPutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lingering Souls");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutEnoughMana() {
        harness.setGraveyard(player1, List.of(new LingeringSouls()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> spiritTokens() {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .toList();
    }
}
