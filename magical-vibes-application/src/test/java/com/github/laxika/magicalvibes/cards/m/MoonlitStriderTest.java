package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoonlitStriderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability grants the chosen color protection to a creature you control")
    void sacAbilityGrantsChosenColorProtection() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Moonlit Strider");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.resetModifiers();
        assertThat(bears.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Soulshift 3 returns a targeted Spirit with mana value 3 or less when Moonlit Strider dies")
    void soulshiftReturnsCheapSpirit() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new MoonlitStrider());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
