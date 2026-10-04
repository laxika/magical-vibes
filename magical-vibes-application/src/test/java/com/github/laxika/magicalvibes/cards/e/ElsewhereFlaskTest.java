package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MistveilPlains;
import com.github.laxika.magicalvibes.cards.p.PrismaticOmen;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElsewhereFlask.class, Forest.class, MistveilPlains.class, PrismaticOmen.class})
class ElsewhereFlaskTest extends BaseCardTest {

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new ElsewhereFlask()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        // One card cast, one drawn: net hand size returns to what it was before casting.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Activating the ability sacrifices Elsewhere Flask")
    void activatingSacrificesFlask() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Elsewhere Flask");
        harness.assertInGraveyard(player1, "Elsewhere Flask");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving prompts the controller for a basic land type choice")
    void resolvingPromptsForChoice() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.OwnLandsBecomeBasicTypeChoice.class);
        assertThat(interaction.options()).containsExactlyInAnyOrder("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
    }

    @Test
    @DisplayName("Every land the controller controls becomes the chosen type")
    void allControllerLandsBecomeChosenType() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        Permanent forestA = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent forestB = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(forestA.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forestB.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Overridden Forest produces blue mana instead of green")
    void overriddenForestProducesBlueMana() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Only the controller's lands are affected, not the opponent's")
    void opponentLandsUnaffected() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.addToBattlefield(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(opponentForest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Override is cleared at end of turn")
    void overrideClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Lands entering after resolution keep their original type")
    void laterLandsAreUnaffected() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");
        Permanent later = harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.effectiveBasicLandTypes(gd, original)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.effectiveBasicLandTypes(gd, later)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Lands entering before resolution are included")
    void landsAreDeterminedAtResolution() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        Permanent land = harness.enterBattlefieldAndReturn(player1, new Forest());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Ability resolves with no controlled lands")
    void resolvesWithoutLands() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.forceActivePlayer(player1);

        activateAndChoose("MOUNTAIN");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Elsewhere Flask");
    }

    @Test
    @DisplayName("Nonbasic lands lose their printed abilities and old land types")
    void nonbasicLandBecomesIsland() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MistveilPlains());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.hasLostPrintedAbilities(gd, land)).isTrue();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.PLAINS);
        assertThat(gqs.hasLostPrintedAbilities(gd, land)).isFalse();
    }

    @Test
    @DisplayName("Prismatic Omen entering later adds all basic land types")
    void laterPrismaticOmenAddsAllBasicLandTypes() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        activateAndChoose("ISLAND");

        harness.setHand(player1, List.of(new PrismaticOmen()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactlyInAnyOrder(
                CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                CardSubtype.MOUNTAIN, CardSubtype.FOREST);
    }

    private void activateAndChoose(String subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }

}
