package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.r.RithsGrove;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaSnidd.class, RithsGrove.class, AlphaKavu.class})
class SeaSniddTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability targets a land")
    void activatingAbilityTargetsLand() {
        Permanent seaSnidd = addCreatureReady(player1, new SeaSnidd());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RithsGrove());
        harness.forceActivePlayer(player1);
        UUID landId = land.getId();

        harness.activateAbility(player1, 0, null, landId);

        assertThat(seaSnidd.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(landId);
    }

    @Test
    @DisplayName("Resolving the ability prompts for a replacing basic land type")
    void resolvingPromptsForChoice() {
        addCreatureReady(player1, new SeaSnidd());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RithsGrove());
        harness.forceActivePlayer(player1);
        UUID landId = land.getId();

        harness.activateAbility(player1, 0, null, landId);
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        assertThat(interaction.options()).containsExactly("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.AddBasicLandTypeChoice.class);
        assertThat(((ChoiceContext.AddBasicLandTypeChoice) interaction.context()).replacing()).isTrue();
    }

    @Test
    @DisplayName("The chosen type replaces the land type and changes its mana")
    void chosenTypeReplacesLandType() {
        Permanent land = becomeIsland(player1);

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.LAIR)).isFalse();

        int landIndex = gd.playerBattlefields.get(player1.getId()).indexOf(land);
        gs.tapPermanent(gd, player1, landIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("The chosen type wears off at end of turn")
    void chosenTypeWearsOffAtEndOfTurn() {
        Permanent land = becomeIsland(player1);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).isEmpty();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.LAIR)).isTrue();
    }

    @Test
    @DisplayName("The ability can target an opponent's land and its controller chooses the type")
    void canTargetOpponentsLand() {
        addCreatureReady(player1, new SeaSnidd());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RithsGrove());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The ability cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new SeaSnidd());
        harness.addToBattlefield(player1, new RithsGrove());
        Permanent kavu = addCreatureReady(player1, new AlphaKavu());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavu.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent becomeIsland(com.github.laxika.magicalvibes.model.Player player) {
        addCreatureReady(player, new SeaSnidd());
        Permanent land = harness.addToBattlefieldAndReturn(player, new RithsGrove());
        harness.forceActivePlayer(player);
        UUID landId = land.getId();

        harness.activateAbility(player, 0, null, landId);
        harness.passBothPriorities();
        harness.handleListChoice(player, "ISLAND");

        return land;
    }
}
