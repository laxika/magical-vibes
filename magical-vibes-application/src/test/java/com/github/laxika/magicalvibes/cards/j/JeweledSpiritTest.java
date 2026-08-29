package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    @DisplayName("The ability cannot be activated without two lands to sacrifice")
    void cannotActivateWithoutTwoLands() {
        addCreatureReady(player1, new JeweledSpirit());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSpiritWithTwoLands() {
        Permanent spirit = addCreatureReady(player1, new JeweledSpirit());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        return spirit;
    }
}
