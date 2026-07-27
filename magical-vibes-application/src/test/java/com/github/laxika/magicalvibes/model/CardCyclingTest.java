package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers {@link Card#addCycling(String)}. Two things about it are load-bearing: cycling must be
 * activatable from hand and nowhere else, and {@link ActivatedAbility#isCyclingAbility()} decides
 * what counts as cycling by reading the description, so the generated text has to be a string the
 * engine actually recognises.
 */
class CardCyclingTest {

    @Test
    @DisplayName("addCycling registers a hand-only ability that discards to draw one card")
    void addCyclingRegistersHandAbility() {
        Card card = new Card();

        card.addCycling("{2}");

        assertThat(card.getHandActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities()).isEmpty();
        assertThat(card.getGraveyardActivatedAbilities()).isEmpty();

        ActivatedAbility ability = card.getHandActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.getEffects()).containsExactly(new DrawCardEffect(1));
        assertThat(ability.getDescription())
                .isEqualTo("Cycling {2} ({2}, Discard this card: Draw a card.)");
    }

    @ParameterizedTest(name = "cycling {0} is recognised as a cycling ability")
    @ValueSource(strings = {"{2}", "{R}", "{1}{U}", "{5}{W}{U}{B}"})
    void generatedDescriptionIsRecognisedAsCycling(String cost) {
        Card card = new Card();

        card.addCycling(cost);

        ActivatedAbility ability = card.getHandActivatedAbilities().getFirst();
        assertThat(ability.getDescription())
                .isEqualTo("Cycling " + cost + " (" + cost + ", Discard this card: Draw a card.)");
        assertThat(ability.isCyclingAbility()).isTrue();
    }
}
