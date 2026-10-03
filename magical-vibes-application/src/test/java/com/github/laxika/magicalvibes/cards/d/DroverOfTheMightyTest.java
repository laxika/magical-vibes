package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DroverOfTheMighty.class, ColossalDreadmaw.class, JungleDelver.class})
class DroverOfTheMightyTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+2 (becomes 3/3) when controller controls a Dinosaur")
    void boostWithDinosaur() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, new ColossalDreadmaw());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(3);
    }

    @Test
    @DisplayName("Base 1/1 without a Dinosaur")
    void noBoostWithoutDinosaur() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost with a non-Dinosaur creature on the battlefield")
    void noBoostWithNonDinosaurCreature() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, new JungleDelver());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses +2/+2 when Dinosaur leaves the battlefield")
    void losesBoostWhenDinosaurLeaves() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, new ColossalDreadmaw());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);

        // Remove the Dinosaur
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard() instanceof ColossalDreadmaw);

        // Boost should be gone immediately (computed on the fly)
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Dinosaur does not grant the boost")
    void opponentDinosaurDoesNotCount() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player2, new ColossalDreadmaw());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for mana prompts color choice")
    void tapForManaPromptsColorChoice() {
        Permanent drover = addCreatureReady(player1, new DroverOfTheMighty());

        harness.activateAbility(player1, 0, null, null);

        assertThat(drover.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Choosing a color adds exactly one mana of that color")
    void choosingColorAddsMana(ManaColor color) {
        Permanent drover = addCreatureReady(player1, new DroverOfTheMighty());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(drover.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Static boost survives end-of-turn modifier reset")
    void staticBoostSurvivesEndOfTurnReset() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        harness.addToBattlefield(player1, new ColossalDreadmaw());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);

        drover.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(3);
    }

    @Test
    void multipleDinosaursGrantOnlyOneBoostAndOneRemainingKeepsIt() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        harness.addToBattlefield(player1, new ColossalDreadmaw());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(3);
    }

    @Test
    void dinosaurEnteringAfterDroverImmediatelyGrantsBoost() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new ColossalDreadmaw());

        assertThat(gqs.getEffectivePower(gd, drover)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, drover)).isEqualTo(3);
    }

    @Test
    void summoningSickDroverCannotProduceMana() {
        Permanent drover = harness.addToBattlefieldAndReturn(player1, new DroverOfTheMighty());
        drover.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drover.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappedDroverCannotProduceManaAgain() {
        Permanent drover = addCreatureReady(player1, new DroverOfTheMighty());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(drover.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
