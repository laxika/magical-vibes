package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathpactAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Dying creates a 1/1 white-and-black Cleric token")
    void dyingCreatesClericToken() {
        killAngel();

        List<Permanent> clerics = findPermanents(player1, "Cleric");
        assertThat(clerics).hasSize(1);

        Permanent cleric = clerics.getFirst();
        assertThat(cleric.getCard().isToken()).isTrue();
        assertThat(cleric.getEffectivePower()).isEqualTo(1);
        assertThat(cleric.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability sacrifices the token and returns Deathpact Angel from the graveyard")
    void tokenAbilityReturnsAngel() {
        killAngel();
        Permanent cleric = readyCleric();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null);
        harness.assertNotOnBattlefield(player1, "Cleric");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, indexInGraveyard(player1, "Deathpact Angel"));

        harness.assertOnBattlefield(player1, "Deathpact Angel");
        harness.assertNotInGraveyard(player1, "Deathpact Angel");
    }

    @Test
    @DisplayName("Token ability returns nothing when no Deathpact Angel is in the graveyard")
    void tokenAbilityWithNoAngelInGraveyard() {
        killAngel();
        Permanent cleric = readyCleric();
        // Remove the Angel card so only an unrelated creature card remains in the graveyard.
        gd.playerGraveyards.get(player1.getId())
                .removeIf(card -> "Deathpact Angel".equals(card.getName()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    /** Wrath of God cast by the opponent kills the Angel; both the death trigger and the token resolve. */
    private void killAngel() {
        harness.addToBattlefield(player1, new DeathpactAngel());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Angel dies, death trigger goes on the stack
        harness.passBothPriorities(); // Death trigger resolves — token is created
    }

    private Permanent readyCleric() {
        Permanent cleric = findPermanents(player1, "Cleric").getFirst();
        cleric.setSummoningSick(false);
        return cleric;
    }

    private int indexInGraveyard(Player player, String cardName) {
        List<com.github.laxika.magicalvibes.model.Card> graveyard = gd.playerGraveyards.get(player.getId());
        for (int i = 0; i < graveyard.size(); i++) {
            if (cardName.equals(graveyard.get(i).getName())) {
                return i;
            }
        }
        throw new IllegalStateException(cardName + " is not in the graveyard");
    }
}
