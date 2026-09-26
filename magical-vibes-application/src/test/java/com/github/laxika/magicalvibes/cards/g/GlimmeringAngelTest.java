package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AgonizingDemise;
import com.github.laxika.magicalvibes.cards.c.CinderShade;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimmeringAngel.class, AgonizingDemise.class, CinderShade.class})
class GlimmeringAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants shroud until end of turn")
    void grantsShroudUntilEndOfTurn() {
        Permanent angel = addCreatureReady(player1, new GlimmeringAngel());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(angel.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent angel = addCreatureReady(player1, new GlimmeringAngel());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(angel.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Ability requires blue mana")
    void requiresBlueMana() {
        addCreatureReady(player1, new GlimmeringAngel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability cannot be paid with colorless mana")
    void cannotPayWithColorlessMana() {
        addCreatureReady(player1, new GlimmeringAngel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Shroud prevents the creature from being targeted by spells")
    void shroudPreventsTargetedSpells() {
        Permanent angel = addCreatureReady(player1, new GlimmeringAngel());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new AgonizingDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents the creature from being targeted by abilities")
    void shroudPreventsTargetedAbilities() {
        addCreatureReady(player1, new CinderShade());
        Permanent angel = addCreatureReady(player1, new GlimmeringAngel());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, angel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
