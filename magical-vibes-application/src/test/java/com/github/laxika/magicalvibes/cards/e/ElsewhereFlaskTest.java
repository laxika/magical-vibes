package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElsewhereFlaskTest extends BaseCardTest {

    // ===== ETB draw =====

    @Test
    @DisplayName("ETB ability draws one card")
    void etbDrawsOneCard() {
        harness.setHand(player1, List.of(new ElsewhereFlask()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact, ETB trigger onto stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
        // One card cast, one drawn: net hand size returns to what it was before casting.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    // ===== Sacrifice ability =====

    @Test
    @DisplayName("Activating the ability sacrifices Elsewhere Flask")
    void activatingSacrificesFlask() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elsewhere Flask"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Elsewhere Flask"));
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
    }

    // ===== Type replacement (rule 305.7) =====

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

        int forestIndex = indexOnBattlefield(player1, "Forest");
        gs.tapPermanent(gd, player1, forestIndex);

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

    // ===== Until end of turn =====

    @Test
    @DisplayName("Override is cleared at end of turn")
    void overrideClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElsewhereFlask());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);

        forest.resetModifiers();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    // ===== Helpers =====

    private void activateAndChoose(String subtype) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }

    private int indexOnBattlefield(com.github.laxika.magicalvibes.model.Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("No " + name + " on battlefield");
    }
}
