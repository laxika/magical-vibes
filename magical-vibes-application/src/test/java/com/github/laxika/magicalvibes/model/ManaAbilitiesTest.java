package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
}
