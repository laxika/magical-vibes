package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the abilities the factories build. The reminder text is written out here rather than
 * derived from {@link ManaColor#getCode()}, so that a mistake in the derivation the factory
 * uses would actually fail this test.
 */
class ManaAbilitiesTest {

    @ParameterizedTest(name = "tapFor({0}) reads \"{1}\"")
    @CsvSource({
            "WHITE,     '{T}: Add {W}.'",
            "BLUE,      '{T}: Add {U}.'",
            "BLACK,     '{T}: Add {B}.'",
            "RED,       '{T}: Add {R}.'",
            "GREEN,     '{T}: Add {G}.'",
            "COLORLESS, '{T}: Add {C}.'",
    })
    void tapForProducesTheColoursManaAndReminderText(ManaColor color, String expectedText) {
        ActivatedAbility ability = ManaAbilities.tapFor(color);

        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).containsExactly(new AwardManaEffect(color));
        assertThat(ability.getDescription()).isEqualTo(expectedText);
    }

    @Test
    @DisplayName("tapForAnyColor() taps for one mana of any color")
    void tapForAnyColor() {
        ActivatedAbility ability = ManaAbilities.tapForAnyColor();

        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).containsExactly(new AwardAnyColorManaEffect());
        assertThat(ability.getDescription()).isEqualTo("{T}: Add one mana of any color.");
    }

    @Test
    @DisplayName("tapFor(color, amount) adds that much mana of the color")
    void tapForAnAmount() {
        ActivatedAbility ability = ManaAbilities.tapFor(ManaColor.COLORLESS, 3);

        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getEffects()).containsExactly(new AwardManaEffect(ManaColor.COLORLESS, 3));
        assertThat(ability.getDescription()).isEqualTo("{T}: Add {C}{C}{C}.");
    }

    @Test
    @DisplayName("A basic land type's intrinsic ability is one shared {T} ability of its color (CR 305.6)")
    void intrinsicAbilityIsSharedPerColor() {
        ActivatedAbility forest = ManaAbilities.intrinsic(ManaColor.GREEN);

        assertThat(forest).isSameAs(ManaAbilities.intrinsic(ManaColor.GREEN));
        assertThat(forest.getEffects()).containsExactly(new AwardManaEffect(ManaColor.GREEN));
        assertThat(forest.getDescription()).isEqualTo("{T}: Add {G}.");
    }

    @Test
    @DisplayName("A land card gets the intrinsic ability of each basic land type it has, in subtype order")
    void landCardGetsTheAbilitiesOfItsBasicLandTypes() {
        Card tundra = land(List.of(CardSubtype.PLAINS, CardSubtype.ISLAND));

        assertThat(ManaAbilities.intrinsic(tundra)).containsExactly(
                ManaAbilities.intrinsic(ManaColor.WHITE), ManaAbilities.intrinsic(ManaColor.BLUE));
    }

    @Test
    @DisplayName("A card that isn't a land has no intrinsic mana ability, whatever its subtypes")
    void nonLandHasNoIntrinsicAbility() {
        Card card = new Card();
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.FOREST));

        assertThat(ManaAbilities.intrinsic(card)).isEmpty();
    }

    @Test
    @DisplayName("withIntrinsic lists printed abilities first and skips an intrinsic one the card already prints")
    void withIntrinsicAddsOnlyTheMissingIntrinsicAbilities() {
        Card land = land(List.of(CardSubtype.PLAINS, CardSubtype.FOREST));
        ActivatedAbility printedWhite = ManaAbilities.tapFor(ManaColor.WHITE);
        ActivatedAbility printedBlack = ManaAbilities.tapFor(ManaColor.BLACK);
        land.addActivatedAbility(printedWhite);
        land.addActivatedAbility(printedBlack);

        assertThat(ManaAbilities.withIntrinsic(land))
                .containsExactly(printedWhite, printedBlack, ManaAbilities.intrinsic(ManaColor.GREEN));
    }

    private static Card land(List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setType(CardType.LAND);
        card.setSubtypes(subtypes);
        return card;
    }
}
