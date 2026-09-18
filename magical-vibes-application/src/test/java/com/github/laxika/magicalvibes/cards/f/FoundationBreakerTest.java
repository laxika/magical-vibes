package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({FoundationBreaker.class, GloriousAnthem.class, GrizzlyBears.class, LeoninScimitar.class})
class FoundationBreakerTest extends BaseCardTest {

    private void castBreaker() {
        harness.setHand(player1, List.of(new FoundationBreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB may destroy an artifact")
    void etbDestroysArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        castBreaker();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Leonin Scimitar"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Foundation Breaker");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Evoke destroys the target and sacrifices Foundation Breaker")
    void evokeDestroysAndSacrificesSelf() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new FoundationBreaker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Foundation Breaker");
        harness.assertInGraveyard(player1, "Foundation Breaker");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the may leaves the enchantment alone")
    void decliningLeavesEnchantment() {
        harness.addToBattlefield(player2, new GloriousAnthem());
        castBreaker();

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Glorious Anthem"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Foundation Breaker");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Only artifacts and enchantments are valid targets")
    void onlyArtifactsAndEnchantmentsAreValidTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LeoninScimitar());
        castBreaker();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactly(harness.getPermanentId(player2, "Leonin Scimitar"));
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FoundationBreaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.getGameService().playCard(
                harness.getGameData(), player1, 0, 0, creatureId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }
}
