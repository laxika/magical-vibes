package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AzoriusSignet;
import com.github.laxika.magicalvibes.cards.b.BronzeBombshell;
import com.github.laxika.magicalvibes.cards.s.SimicGuildmage;
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

@CardUsed({MightOfTheNephilim.class, SimicGuildmage.class, MistralCharger.class,
        BronzeBombshell.class, AzoriusSignet.class})
class MightOfTheNephilimTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+2 for each of its colors")
    void boostsByColorCount() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new SimicGuildmage());
        int basePower = gqs.getEffectivePower(gd, guildmage);
        int baseToughness = gqs.getEffectiveToughness(gd, guildmage);

        castMight(guildmage);

        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(baseToughness + 4);
    }

    @Test
    @DisplayName("A monocolored creature gets +2/+2")
    void boostsMonocoloredCreature() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        int basePower = gqs.getEffectivePower(gd, charger);
        int baseToughness = gqs.getEffectiveToughness(gd, charger);

        castMight(charger);

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("A colorless creature gets no boost")
    void doesNotBoostColorlessCreature() {
        Permanent bombshell = harness.addToBattlefieldAndReturn(player1, new BronzeBombshell());
        int basePower = gqs.getEffectivePower(gd, bombshell);
        int baseToughness = gqs.getEffectiveToughness(gd, bombshell);

        castMight(bombshell);

        assertThat(gqs.getEffectivePower(gd, bombshell)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bombshell)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The boost ends at end of turn")
    void boostEndsAtEndOfTurn() {
        Permanent charger = harness.addToBattlefieldAndReturn(player1, new MistralCharger());
        int basePower = gqs.getEffectivePower(gd, charger);
        int baseToughness = gqs.getEffectiveToughness(gd, charger);

        castMight(charger);
        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower + 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, charger)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, charger)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent signet = harness.addToBattlefieldAndReturn(player1, new AzoriusSignet());
        harness.setHand(player1, List.of(new MightOfTheNephilim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can boost an opponent's creature based on that creature's colors")
    void boostsOpponentsCreature() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player2, new SimicGuildmage());
        int basePower = gqs.getEffectivePower(gd, guildmage);
        int baseToughness = gqs.getEffectiveToughness(gd, guildmage);

        castMight(guildmage);

        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(basePower + 4);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(baseToughness + 4);
    }

    @Test
    @DisplayName("Multiple copies give cumulative boosts until end of turn")
    void multipleBoostsAccumulateAndExpire() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new SimicGuildmage());
        int basePower = gqs.getEffectivePower(gd, guildmage);
        int baseToughness = gqs.getEffectiveToughness(gd, guildmage);

        castMight(guildmage);
        castMight(guildmage);

        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(basePower + 8);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(baseToughness + 8);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(baseToughness);
    }

    private void castMight(Permanent creature) {
        harness.setHand(player1, List.of(new MightOfTheNephilim()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }
}
