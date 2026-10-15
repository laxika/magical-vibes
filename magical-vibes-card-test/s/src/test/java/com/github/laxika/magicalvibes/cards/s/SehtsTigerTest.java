package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EmberwildeAugur;
import com.github.laxika.magicalvibes.cards.r.RiddleOfLightning;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SehtsTiger.class, RiddleOfLightning.class, EmberwildeAugur.class, SliverLegion.class})
class SehtsTigerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets its controller choose a color for protection")
    void grantsControllerProtectionFromChosenColor() {
        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.playerHasProtectionFromColor(gd, player1.getId(), CardColor.RED)).isTrue();
        assertThat(gqs.playerHasProtectionFromColor(gd, player1.getId(), CardColor.BLUE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> assertThat(gqs.hasProtectionFrom(gd, permanent, CardColor.RED))
                        .isFalse());
    }

    @Test
    @DisplayName("Protection from the chosen color prevents that color from targeting its controller")
    void chosenColorProtectionPreventsTargetingController() {
        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new RiddleOfLightning()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from the chosen color prevents combat damage from that color")
    void chosenColorProtectionPreventsCombatDamage() {
        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "RED");

        addCreatureReady(player2, new EmberwildeAugur());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Controller protection expires at end of turn")
    void controllerProtectionExpiresAtEndOfTurn() {
        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.playerHasProtectionFromColor(gd, player1.getId(), CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.playerHasProtectionFromColor(gd, player1.getId(), CardColor.BLUE)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Protection prevents combat damage from a source with any matching color")
    void protectionPreventsMulticoloredCombatDamage(CardColor chosenColor) {
        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, chosenColor.name());

        addCreatureReady(player2, new SliverLegion());
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flash can protect the controller from a red ability already on the stack")
    void flashMakesExistingAbilityTargetIllegal() {
        addCreatureReady(player2, new EmberwildeAugur());
        advanceToUpkeep(player2);
        harness.activateAbility(player2, 0, null, player1.getId());

        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Protection from blue does not prevent a red ability from dealing damage")
    void otherColorAbilityStillDealsDamage() {
        addCreatureReady(player2, new EmberwildeAugur());
        advanceToUpkeep(player2);
        harness.activateAbility(player2, 0, null, player1.getId());

        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "BLUE");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Separate Tigers grant protection from both chosen colors only to their controller")
    void multipleProtectionChoicesAccumulate() {
        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "RED");
        harness.castFromHand(player1, new SehtsTiger(), "{2}{W}{W}");
        resolveAllTriggers();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.playerHasProtectionFromColor(gd, player1.getId(), CardColor.RED)).isTrue();
        assertThat(gqs.playerHasProtectionFromColor(gd, player1.getId(), CardColor.BLUE)).isTrue();
        assertThat(gqs.playerHasProtectionFromColor(gd, player2.getId(), CardColor.RED)).isFalse();
        assertThat(gqs.playerHasProtectionFromColor(gd, player2.getId(), CardColor.BLUE)).isFalse();
    }
}
