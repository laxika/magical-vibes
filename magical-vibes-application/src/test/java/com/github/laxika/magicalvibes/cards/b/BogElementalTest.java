package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MagetasBoon;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogElemental.class, BogGlider.class, MagetasBoon.class, RhysticCave.class})
class BogElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Auto-sacrifices when its controller has no land")
    void autoSacrificesWithoutLand() {
        harness.addToBattlefield(player1, new BogElemental());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Elemental");
        harness.assertInGraveyard(player1, "Bog Elemental");
    }

    @Test
    @DisplayName("Sacrificing a land keeps Bog Elemental")
    void sacrificingLandKeepsElemental() {
        harness.addToBattlefield(player1, new BogElemental());
        harness.addToBattlefield(player1, new RhysticCave());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID rhysticCaveId = findPermanent(player1, "Rhystic Cave").getId();
        harness.handlePermanentChosen(player1, rhysticCaveId);

        harness.assertOnBattlefield(player1, "Bog Elemental");
        harness.assertNotOnBattlefield(player1, "Rhystic Cave");
        harness.assertInGraveyard(player1, "Rhystic Cave");
    }

    @Test
    @DisplayName("Declining to sacrifice a land sacrifices Bog Elemental")
    void decliningSacrificesElemental() {
        harness.addToBattlefield(player1, new BogElemental());
        harness.addToBattlefield(player1, new RhysticCave());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Bog Elemental");
        harness.assertInGraveyard(player1, "Bog Elemental");
        harness.assertOnBattlefield(player1, "Rhystic Cave");
    }

    @Test
    @DisplayName("An opponent's land does not satisfy the requirement")
    void opponentLandDoesNotCount() {
        harness.addToBattlefield(player1, new BogElemental());
        harness.addToBattlefield(player2, new RhysticCave());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Elemental");
        harness.assertInGraveyard(player1, "Bog Elemental");
        harness.assertOnBattlefield(player2, "Rhystic Cave");
    }

    @Test
    @DisplayName("A nonland permanent does not satisfy the requirement")
    void nonlandPermanentDoesNotCount() {
        harness.addToBattlefield(player1, new BogElemental());
        harness.addToBattlefield(player1, new BogGlider());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Elemental");
        harness.assertInGraveyard(player1, "Bog Elemental");
        harness.assertOnBattlefield(player1, "Bog Glider");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Protection from white prevents a white Aura from targeting Bog Elemental")
    void protectionFromWhitePreventsWhiteAura() {
        harness.addToBattlefield(player1, new BogElemental());
        UUID bogElementalId = findPermanent(player1, "Bog Elemental").getId();

        harness.setHand(player2, List.of(new MagetasBoon()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, bogElementalId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
        harness.assertOnBattlefield(player1, "Bog Elemental");
    }
}
