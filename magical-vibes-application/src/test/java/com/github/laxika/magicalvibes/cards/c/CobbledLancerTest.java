package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CobbledLancer.class, ThinkTwice.class})
class CobbledLancerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cobbled Lancer exiles a creature card from graveyard")
    void castingExilesCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cobbled Lancer");

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cobbled Lancer"));
    }

    @Test
    @DisplayName("Cannot cast without a creature in graveyard")
    void cannotCastWithoutCreatureInGraveyard() {
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a non-creature card from graveyard")
    void cannotExileNonCreatureCard() {
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Resolves onto the battlefield after paying exile cost")
    void entersBattlefieldAfterExileCost() {
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cobbled Lancer");
    }

    @Test
    @DisplayName("Graveyard ability exiles the source and draws a card")
    void graveyardAbilityExilesAndDraws() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.setLibrary(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Cobbled Lancer");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cobbled Lancer"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Cobbled Lancer");
    }

    @Test
    void castingExilesOnlyTheSelectedCreature() {
        ThinkTwice instant = new ThinkTwice();
        CobbledLancer selected = new CobbledLancer();
        CobbledLancer remaining = new CobbledLancer();
        harness.setGraveyard(player1, List.of(instant, selected, remaining));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(selected);
    }

    @Test
    void opponentsGraveyardCannotPayTheCastingCost() {
        harness.setGraveyard(player2, List.of(new CobbledLancer()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player2, "Cobbled Lancer");
        harness.assertInHand(player1, "Cobbled Lancer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotOmitTheAdditionalCastingCost() {
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.setHand(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Cobbled Lancer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardAbilityRequiresBlueMana() {
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Cobbled Lancer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardAbilityRequiresFourMana() {
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Cobbled Lancer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void graveyardAbilityCanBeActivatedOnOpponentsTurnAndDrawsOnlyOnResolution() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new CobbledLancer()));
        harness.setLibrary(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Cobbled Lancer");
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Think Twice");
    }
}
