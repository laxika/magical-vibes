package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticCompass.class, Forest.class, GrizzlyBears.class, CityOfBrass.class})
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
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("Cannot activate without the {1} mana")
    void cannotActivateWithoutMana() {
        Permanent compass = addReadyCompass(player1);
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(compass.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    // ===== Type replacement (rule 305.7) =====

    @Test
    @DisplayName("Chosen type replaces the land's subtypes and its mana ability")
    void chosenTypeReplacesSubtypes() {
        Permanent forest = becomeIsland(player1);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Overridden Forest produces blue mana instead of green")
    void overriddenForestProducesBlueMana() {
        becomeIsland(player1);

        int forestIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest")));
        harness.tapPermanent(player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    // ===== Until end of turn =====

    @Test
    @DisplayName("Override is cleared at end of turn")
    void overrideClearedAtEndOfTurn() {
        Permanent forest = becomeIsland(player1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Replacing a nonbasic land removes its old abilities")
    void replacingNonbasicLandRemovesOldAbilities() {
        Permanent city = becomeIsland(player1, new CityOfBrass());
        harness.setLife(player1, 20);

        int cityIndex = gd.playerBattlefields.get(player1.getId()).indexOf(city);
        int blueBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);
        harness.tapPermanent(player1, cityIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(blueBefore + 1);
        resolveAllTriggers();
        harness.assertLife(player1, 20);
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
    void abilityControllerChoosesTypeForOpponentLand() {
        addReadyCompass(player1);
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        UUID opponentForestId = harness.getPermanentId(player2, Forest.class.getSimpleName());

        harness.activateAbility(player1, 0, null, opponentForestId);
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());

        harness.handleListChoice(player1, CardSubtype.ISLAND.name());

        Permanent forest = gqs.findPermanentById(gd, opponentForestId);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
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
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MysticCompass());
        perm.setSummoningSick(false);
        return perm;
    }

    /** Adds a ready Mystic Compass + land for {@code player}, then makes the land become an Island. */
    private Permanent becomeIsland(Player player) {
        return becomeIsland(player, new Forest());
    }

    private Permanent becomeIsland(Player player, Card land) {
        addReadyCompass(player);
        harness.addToBattlefield(player, land);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player);
        UUID landId = harness.getPermanentId(player, land.getName());

        harness.activateAbility(player, 0, null, landId);
        harness.passBothPriorities();
        harness.handleListChoice(player, CardSubtype.ISLAND.name());

        return gqs.findPermanentById(gd, landId);
    }
}
