package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JeweledSpirit.class, WintermoonMesa.class})
class JeweledSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing two lands grants protection from a chosen color until end of turn")
    void sacrificesTwoLandsAndGrantsChosenColorProtection() {
        Permanent spirit = addSpiritWithTwoLands();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(spirit.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("The ability can grant protection from artifacts")
    void grantsProtectionFromArtifacts() {
        Permanent spirit = addSpiritWithTwoLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(spirit.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent spirit = addSpiritWithTwoLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.BLUE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without two lands to sacrifice")
    void cannotActivateWithoutTwoLands() {
        addCreatureReady(player1, new JeweledSpirit());
        harness.addToBattlefield(player1, new WintermoonMesa());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSpiritWithTwoLands() {
        Permanent spirit = addCreatureReady(player1, new JeweledSpirit());
        harness.addToBattlefield(player1, new WintermoonMesa());
        harness.addToBattlefield(player1, new WintermoonMesa());
        return spirit;
    }
}
