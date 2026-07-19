package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrixisIllusionistTest extends BaseCardTest {

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land you control")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new GrixisIllusionist());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(forestId);
    }

    // ===== Type replacement (rule 305.7) =====

    @Test
    @DisplayName("Chosen type overrides the land to the new basic type only (type-replacing)")
    void chosenTypeReplacesSubtypes() {
        Permanent forest = becomeIsland(player1);

        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Overridden Forest produces blue mana instead of green")
    void overriddenForestProducesBlueMana() {
        becomeIsland(player1);

        int forestIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest")));
        gs.tapPermanent(gd, player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    // ===== Until end of turn =====

    @Test
    @DisplayName("Override is cleared at end of turn")
    void overrideClearedAtEndOfTurn() {
        Permanent forest = becomeIsland(player1);
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

        forest.resetModifiers();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    // ===== Targeting restrictions ("land you control") =====

    @Test
    @DisplayName("Cannot target a land controlled by the opponent")
    void cannotTargetOpponentLand() {
        addCreatureReady(player1, new GrixisIllusionist());
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new GrixisIllusionist());
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Helpers =====

    /** Adds a Grixis Illusionist + Forest for {@code player}, then makes the Forest become an Island. */
    private Permanent becomeIsland(com.github.laxika.magicalvibes.model.Player player) {
        addCreatureReady(player, new GrixisIllusionist());
        harness.addToBattlefield(player, new Forest());
        harness.forceActivePlayer(player);
        UUID forestId = harness.getPermanentId(player, "Forest");

        harness.activateAbility(player, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player, "ISLAND");

        return gqs.findPermanentById(gd, forestId);
    }
}
