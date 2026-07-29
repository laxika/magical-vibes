package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers {@link Card#addUnearth}, {@link Card#addEmbalm} and {@link Card#addEternalize}.
 *
 * <p>The reminder strings are written out in full rather than rebuilt the way the helpers do, so
 * that they pin the exact text the cards carried before they were folded onto these helpers.
 */
class CardGraveyardKeywordTest {

    @Test
    @DisplayName("addUnearth returns the card with haste and exiles it at end of turn, sorcery speed")
    void unearth() {
        Card card = new Card();

        card.addUnearth("{B}");

        assertThat(card.getGraveyardActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities()).isEmpty();
        assertThat(card.getHandActivatedAbilities()).isEmpty();

        ActivatedAbility ability = card.getGraveyardActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{B}");
        assertThat(ability.getDescription()).isEqualTo("Unearth {B}");
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).containsExactly(ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsSelfPredicate())
                .returnAll(true)
                .grantHaste(true)
                .exileAtEndStep(true)
                .exileIfLeavesBattlefield(true)
                .build());
        // unearth returns the card itself, it does not make a token copy
        assertThat(ability.isEmbalmOrEternalize()).isFalse();
    }

    @Test
    @DisplayName("addEmbalm exiles the card for a white Zombie token copy, sorcery speed")
    void embalm() {
        Card card = new Card();

        card.addEmbalm("{3}{W}", "Human Cleric");

        ActivatedAbility ability = card.getGraveyardActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{3}{W}");
        assertThat(ability.getEffects()).containsExactly(
                new ExileSelfFromGraveyardCost(),
                new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true));
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.isEmbalmOrEternalize()).isTrue();
        assertThat(ability.getDescription()).isEqualTo(
                "Embalm {3}{W} ({3}{W}, Exile this card from your graveyard: Create a token that's a copy of it, "
                        + "except it's a white Zombie Human Cleric with no mana cost. Embalm only as a sorcery.)");
    }

    @Test
    @DisplayName("addEternalize is embalm except the token is a 4/4 black Zombie")
    void eternalize() {
        Card card = new Card();

        card.addEternalize("{3}{W}{W}", "Cat");

        ActivatedAbility ability = card.getGraveyardActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{3}{W}{W}");
        assertThat(ability.getEffects()).containsExactly(
                new ExileSelfFromGraveyardCost(),
                new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4));
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.isEmbalmOrEternalize()).isTrue();
        assertThat(ability.getDescription()).isEqualTo(
                "Eternalize {3}{W}{W} ({3}{W}{W}, Exile this card from your graveyard: Create a token that's a copy "
                        + "of it, except it's a 4/4 black Zombie Cat with no mana cost. Eternalize only as a sorcery.)");
    }
}
