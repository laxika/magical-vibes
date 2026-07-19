package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnstableFrontierTest extends BaseCardTest {

    // Unstable Frontier is added first, so it is permanent index 0 on its controller's battlefield.
    private static final int FRONTIER = 0;
    private static final int ADD_COLORLESS = 0;
    private static final int BECOME_TYPE = 1;

    // ===== {T}: Add {C}. =====

    @Test
    @DisplayName("First ability adds one colorless mana")
    void firstAbilityAddsColorless() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, FRONTIER, ADD_COLORLESS, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== {T}: Target land you control becomes the chosen basic land type =====

    @Test
    @DisplayName("Second ability puts it on the stack targeting a land you control")
    void secondAbilityTargetsOwnLand() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, forestId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("Chosen type replaces the land's subtype and mana (rule 305.7)")
    void chosenTypeOverridesSubtypesAndMana() {
        Permanent forest = becomeIsland();

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();

        int forestIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest")));
        gs.tapPermanent(gd, player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Override is cleared at end of turn")
    void overrideClearedAtEndOfTurn() {
        Permanent forest = becomeIsland();
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

        forest.resetModifiers();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    // ===== Targeting restriction ("land you control") =====

    @Test
    @DisplayName("Cannot target a land controlled by the opponent")
    void cannotTargetOpponentLand() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, opponentForestId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    // ===== Helpers =====

    /** Makes player1 control a Forest that becomes an Island via Unstable Frontier's second ability. */
    private Permanent becomeIsland() {
        harness.addToBattlefield(player1, new UnstableFrontier());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, FRONTIER, BECOME_TYPE, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        return gqs.findPermanentById(gd, forestId);
    }
}
