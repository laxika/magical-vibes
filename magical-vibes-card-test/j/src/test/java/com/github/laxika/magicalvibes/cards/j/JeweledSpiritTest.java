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
        harness.passUntil(TurnStep.UPKEEP);

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

    @Test
    @DisplayName("Protection from artifacts wears off at end of turn")
    void artifactProtectionWearsOffAtEndOfTurn() {
        Permanent spirit = addSpiritWithTwoLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(spirit.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(spirit.getProtectionFromCardTypes()).doesNotContain(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Opposing lands cannot pay the sacrifice cost")
    void cannotSacrificeOpposingLands() {
        addCreatureReady(player1, new JeweledSpirit());
        harness.addToBattlefield(player1, new WintermoonMesa());
        harness.addToBattlefield(player2, new WintermoonMesa());
        harness.addToBattlefield(player2, new WintermoonMesa());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A tapped summoning-sick Spirit can activate the ability")
    void tappedSummoningSickSpiritCanActivate() {
        Permanent spirit = addSpiritWithTwoLands();
        spirit.tap();
        spirit.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.GREEN.name());

        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.GREEN)).isTrue();
        assertThat(spirit.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Repeated activations retain each chosen protection")
    void repeatedActivationsAccumulateProtection() {
        Permanent spirit = addSpiritWithTwoLands();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        harness.addToBattlefield(player1, new WintermoonMesa());
        harness.addToBattlefield(player1, new WintermoonMesa());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.RED)).isTrue();
        assertThat(spirit.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    private Permanent addSpiritWithTwoLands() {
        Permanent spirit = addCreatureReady(player1, new JeweledSpirit());
        harness.addToBattlefield(player1, new WintermoonMesa());
        harness.addToBattlefield(player1, new WintermoonMesa());
        return spirit;
    }
}
