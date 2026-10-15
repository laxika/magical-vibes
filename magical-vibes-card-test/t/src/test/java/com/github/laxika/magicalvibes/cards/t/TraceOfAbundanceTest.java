package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TraceOfAbundance.class, Forest.class, GrizzlyBears.class})
class TraceOfAbundanceTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping enchanted land adds one extra mana of the chosen color")
    void enchantedLandAddsExtraManaOfChosenColor() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TraceOfAbundance());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the enchanted land gets the extra mana")
    void onlyEnchantedLandGetsBonus() {
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TraceOfAbundance());
        aura.setAttachedTo(firstForest.getId());

        // Tap the second (non-enchanted) Forest at index 1.
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted land has shroud")
    void enchantedLandHasShroud() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TraceOfAbundance());
        aura.setAttachedTo(forest.getId());

        assertThat(gqs.hasKeyword(gd, forest, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud is lost once Trace of Abundance leaves the battlefield")
    void shroudLostWhenAuraRemoved() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TraceOfAbundance());
        aura.setAttachedTo(forest.getId());

        assertThat(gqs.hasKeyword(gd, forest, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, forest, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cannot cast Trace of Abundance targeting a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new Forest()); // valid target so spell is playable
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new TraceOfAbundance()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The land controller chooses any of the five colors without using the stack")
    void canChooseEveryColor(ManaColor color) {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TraceOfAbundance());
        aura.setAttachedTo(forest.getId());

        harness.tapPermanent(player1, 0);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(color == ManaColor.GREEN ? 2 : 1);
        if (color != ManaColor.GREEN) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("The Aura resolves on either player's land and awards mana to that land's controller")
    void resolvesAndBenefitsLandController(boolean opponentsLand) {
        var landController = opponentsLand ? player2 : player1;
        Permanent forest = harness.addToBattlefieldAndReturn(landController, new Forest());
        harness.setHand(player1, List.of(new TraceOfAbundance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Trace of Abundance");
        assertThat(aura.getAttachedTo()).isEqualTo(forest.getId());
        assertThat(gqs.hasKeyword(gd, forest, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.SHROUD)).isFalse();

        harness.tapPermanent(landController, 0);
        harness.handleListChoice(landController, "BLACK");

        assertThat(gd.playerManaPools.get(landController.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(landController.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        if (opponentsLand) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Shroud prevents both players from targeting the enchanted land")
    void shroudPreventsEitherPlayerTargeting(boolean opponentCasts) {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new TraceOfAbundance());
        aura.setAttachedTo(forest.getId());
        var caster = opponentCasts ? player2 : player1;
        harness.setHand(caster, List.of(new TraceOfAbundance()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.GREEN, 1);
        harness.forceActivePlayer(caster);

        assertThatThrownBy(() -> harness.castEnchantment(caster, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
