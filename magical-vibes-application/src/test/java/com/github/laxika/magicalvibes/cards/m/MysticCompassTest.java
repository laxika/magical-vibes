package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MysticCompassTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating taps the compass, spends the mana, and puts the ability on the stack")
    void activatingTapsAndPutsOnStack() {
        Permanent compass = addReadyCompass(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);

        assertThat(compass.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("Cannot activate without the {1} mana")
    void cannotActivateWithoutMana() {
        addReadyCompass(player1);
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Type replacement (rule 305.7) =====

    @Test
    @DisplayName("Chosen type replaces the land's subtypes and its mana ability")
    void chosenTypeReplacesSubtypes() {
        Permanent forest = becomeIsland(player1);

        GameQueryService.StaticBonus bonus = gqs.computeStaticBonus(gd, forest);
        assertThat(bonus.landSubtypeOverriding()).isTrue();
        assertThat(bonus.grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
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

    // ===== Targeting =====

    @Test
    @DisplayName("Can target a land controlled by the opponent")
    void canTargetOpponentLand() {
        addReadyCompass(player1);
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, null, opponentForestId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(opponentForestId);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addReadyCompass(player1);
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== Helpers =====

    private Permanent addReadyCompass(Player player) {
        Permanent perm = new Permanent(new MysticCompass());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /** Adds a ready Mystic Compass + Forest for {@code player}, then makes the Forest become an Island. */
    private Permanent becomeIsland(Player player) {
        addReadyCompass(player);
        harness.addToBattlefield(player, new Forest());
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player);
        UUID forestId = harness.getPermanentId(player, "Forest");

        harness.activateAbility(player, 0, null, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player, "ISLAND");

        return gqs.findPermanentById(gd, forestId);
    }
}
