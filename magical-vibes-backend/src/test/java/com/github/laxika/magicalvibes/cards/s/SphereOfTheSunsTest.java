package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithFixedChargeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromSourceCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SphereOfTheSunsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Enters tapped flag is set")
    void entersTapped() {
        SphereOfTheSuns card = new SphereOfTheSuns();

        assertThat(card.isEntersTapped()).isTrue();
    }

    @Test
    @DisplayName("Has ETB effect for entering with 3 charge counters")
    void hasEnterWithChargeCountersEffect() {
        SphereOfTheSuns card = new SphereOfTheSuns();

        assertThat(card.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(com.github.laxika.magicalvibes.model.EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(EnterWithFixedChargeCountersEffect.class);
        EnterWithFixedChargeCountersEffect effect = (EnterWithFixedChargeCountersEffect) card.getEffects(
                com.github.laxika.magicalvibes.model.EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has activated ability: tap + remove charge counter to add mana of any color")
    void hasActivatedAbility() {
        SphereOfTheSuns card = new SphereOfTheSuns();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof RemoveChargeCountersFromSourceCost rc && rc.count() == 1)
                .anyMatch(e -> e instanceof AwardAnyColorManaEffect);
    }

    // ===== Entering the battlefield =====

    @Test
    @DisplayName("Enters the battlefield tapped with 3 charge counters")
    void entersWithThreeChargeCountersTapped() {
        harness.setHand(player1, List.of(new SphereOfTheSuns()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent sphere = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sphere of the Suns"))
                .findFirst().orElseThrow();
        assertThat(sphere.getChargeCounters()).isEqualTo(3);
        assertThat(sphere.isTapped()).isTrue();
    }

    // ===== Activated ability: add mana of any color =====

    @Test
    @DisplayName("Activating ability removes a charge counter and prompts for mana color")
    void activateRemovesCounterAndPromptsForColor() {
        harness.addToBattlefield(player1, new SphereOfTheSuns());

        Permanent sphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        sphere.setChargeCounters(3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(sphere.getChargeCounters()).isEqualTo(2);
        assertThat(sphere.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.awaitingColorChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new SphereOfTheSuns());
            GameData gd = harness.getGameData();
            Permanent sphere = gd.playerBattlefields.get(player1.getId()).getFirst();
            sphere.setChargeCounters(3);
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = gd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleColorChosen(player1, color);

            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(gd.interaction.awaitingInputType()).isNull();
        }
    }

    @Test
    @DisplayName("Can activate three times with 3 charge counters (untapping between uses)")
    void canActivateThreeTimes() {
        harness.addToBattlefield(player1, new SphereOfTheSuns());

        Permanent sphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        sphere.setChargeCounters(3);

        // First activation
        harness.activateAbility(player1, 0, null, null);
        harness.handleColorChosen(player1, "RED");
        sphere.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, null);
        harness.handleColorChosen(player1, "BLUE");
        sphere.untap();

        // Third activation
        harness.activateAbility(player1, 0, null, null);
        harness.handleColorChosen(player1, "GREEN");

        assertThat(sphere.getChargeCounters()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        harness.addToBattlefield(player1, new SphereOfTheSuns());

        Permanent sphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        sphere.setChargeCounters(0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        harness.addToBattlefield(player1, new SphereOfTheSuns());

        Permanent sphere = gd.playerBattlefields.get(player1.getId()).getFirst();
        sphere.setChargeCounters(3);

        // First activation taps it
        harness.activateAbility(player1, 0, null, null);
        harness.handleColorChosen(player1, "WHITE");

        // Cannot activate again while tapped
        assertThat(sphere.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }
}
