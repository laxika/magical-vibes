package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.m.MilitaryIntelligence;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PerilousVault.class, Forest.class, RuneclawBear.class, AjaniSteadfast.class, MilitaryIntelligence.class, DarksteelCitadel.class})
class PerilousVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving exiles all nonland permanents from both battlefields")
    void exilesAllNonlandPermanents() {
        addReadyVault(player1);
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"));
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The Vault itself is exiled as a cost, not put into the graveyard")
    void vaultExiledAsCost() {
        addReadyVault(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Perilous Vault");
        harness.assertNotInGraveyard(player1, "Perilous Vault");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Perilous Vault"));
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyVault(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhenTapped() {
        Permanent vault = addReadyVault(player1);
        vault.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles artifacts, enchantments, and planeswalkers without destroying them")
    void exilesEveryNonlandPermanentType() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new PerilousVault());
        harness.addToBattlefield(player2, new MilitaryIntelligence());
        harness.addToBattlefield(player2, new AjaniSteadfast());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName()).containsExactly("Perilous Vault", "Perilous Vault");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactlyInAnyOrder("Military Intelligence", "Ajani Steadfast");
        harness.assertNotInGraveyard(player2, "Military Intelligence");
        harness.assertNotInGraveyard(player2, "Ajani Steadfast");
    }

    @Test
    @DisplayName("Determines what to exile on resolution, after the source is already gone")
    void includesPermanentsThatEnterAfterActivation() {
        harness.addToBattlefield(player1, new PerilousVault());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Perilous Vault");
        harness.addToBattlefield(player2, new MilitaryIntelligence());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player2, "Military Intelligence");
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactlyInAnyOrder("Runeclaw Bear", "Military Intelligence");
    }

    @Test
    @DisplayName("Leaves artifact lands on the battlefield")
    void doesNotExileArtifactLands() {
        addReadyVault(player1);
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Darksteel Citadel");
        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Darksteel Citadel"));
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Darksteel Citadel"));
    }

    private Permanent addReadyVault(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new PerilousVault());
        perm.setSummoningSick(false);
        return perm;
    }
}
