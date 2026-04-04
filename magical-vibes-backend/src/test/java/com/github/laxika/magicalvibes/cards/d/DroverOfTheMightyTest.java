package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DroverOfTheMightyTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC ControlsSubtypeConditionalEffect(DINOSAUR) wrapping StaticBoostEffect(2, 2, SELF)")
    void hasCorrectStaticEffect() {
        DroverOfTheMighty card = new DroverOfTheMighty();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ControlsSubtypeConditionalEffect.class);

        ControlsSubtypeConditionalEffect conditional =
                (ControlsSubtypeConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.DINOSAUR);
        assertThat(conditional.wrapped()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect boost = (StaticBoostEffect) conditional.wrapped();
        assertThat(boost.powerBoost()).isEqualTo(2);
        assertThat(boost.toughnessBoost()).isEqualTo(2);
        assertThat(boost.scope()).isEqualTo(GrantScope.SELF);
    }

    @Test
    @DisplayName("Has activated ability: {T}: Add one mana of any color")
    void hasCorrectManaAbility() {
        DroverOfTheMighty card = new DroverOfTheMighty();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AwardAnyColorManaEffect.class);
    }

    // ===== Conditional boost with Dinosaur =====

    @Test
    @DisplayName("Gets +2/+2 (becomes 3/3) when controller controls a Dinosaur")
    void boostWithDinosaur() {
        harness.addToBattlefield(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent drover = findPermanent(player1, "Drover of the Mighty");
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(3);
    }

    @Test
    @DisplayName("Base 1/1 without a Dinosaur")
    void noBoostWithoutDinosaur() {
        harness.addToBattlefield(player1, new DroverOfTheMighty());

        Permanent drover = findPermanent(player1, "Drover of the Mighty");
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-Dinosaur creature on the battlefield")
    void noBoostWithNonDinosaurCreature() {
        harness.addToBattlefield(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent drover = findPermanent(player1, "Drover of the Mighty");
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    // ===== Loses boost when Dinosaur leaves =====

    @Test
    @DisplayName("Loses +2/+2 when Dinosaur leaves the battlefield")
    void losesBoostWhenDinosaurLeaves() {
        harness.addToBattlefield(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent drover = findPermanent(player1, "Drover of the Mighty");
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);

        // Remove the Dinosaur
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.DINOSAUR));

        // Boost should be gone immediately (computed on the fly)
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    // ===== Opponent's Dinosaur doesn't count =====

    @Test
    @DisplayName("Opponent's Dinosaur does not grant the boost")
    void opponentDinosaurDoesNotCount() {
        harness.addToBattlefield(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player2, createDinosaur());

        Permanent drover = findPermanent(player1, "Drover of the Mighty");
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana prompts color choice")
    void tapForManaPromptsColorChoice() {
        harness.addToBattlefield(player1, new DroverOfTheMighty());
        Permanent drover = findPermanent(player1, "Drover of the Mighty");
        drover.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(drover.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana() {
        for (String color : List.of("WHITE", "BLUE", "BLACK", "RED", "GREEN")) {
            harness = new GameTestHarness();
            player1 = harness.getPlayer1();
            harness.skipMulligan();

            harness.addToBattlefield(player1, new DroverOfTheMighty());
            GameData localGd = harness.getGameData();
            Permanent drover = localGd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Drover of the Mighty"))
                    .findFirst().orElseThrow();
            drover.setSummoningSick(false);
            ManaColor manaColor = ManaColor.valueOf(color);

            harness.activateAbility(player1, 0, null, null);
            int before = localGd.playerManaPools.get(player1.getId()).get(manaColor);

            harness.handleListChoice(player1, color);

            assertThat(localGd.playerManaPools.get(player1.getId()).get(manaColor)).isEqualTo(before + 1);
            assertThat(localGd.interaction.awaitingInputType()).isNull();
        }
    }

    // ===== Static boost survives end-of-turn reset =====

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, createDinosaur());

        Permanent drover = findPermanent(player1, "Drover of the Mighty");
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);

        drover.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(3);
    }

    // ===== Helper methods =====

    private Card createDinosaur() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.DINOSAUR));
        return card;
    }

}
