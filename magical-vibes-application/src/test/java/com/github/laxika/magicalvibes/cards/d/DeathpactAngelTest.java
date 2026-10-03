package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathpactAngel.class, WrathOfGod.class, GrizzlyBears.class})
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

    @Test
    void clericHasBothColorsAndClericSubtype() {
        killAngel();

        Permanent cleric = findPermanent(player1, "Cleric");
        assertThat(cleric.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(cleric.getCard().getSubtypes()).containsExactly(CardSubtype.CLERIC);
    }

    @Test
    void returningAnAvailableAngelCannotBeDeclined() {
        killAngel();
        Permanent cleric = readyCleric();
        payForCleric();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, indexInGraveyard(player1, "Deathpact Angel"));
        harness.assertOnBattlefield(player1, "Deathpact Angel");
    }

    @Test
    void newlyCreatedClericCannotPayTapCost() {
        killAngel();
        Permanent cleric = findPermanent(player1, "Cleric");
        payForCleric();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");
        harness.assertOnBattlefield(player1, "Cleric");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedClericCannotActivate() {
        killAngel();
        Permanent cleric = readyCleric();
        cleric.setTapped(true);
        payForCleric();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("tapped");
        harness.assertOnBattlefield(player1, "Cleric");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenCannotReturnAnAngelFromOpponentsGraveyard() {
        killAngel();
        Permanent cleric = readyCleric();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new DeathpactAngel()));
        payForCleric();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Cleric");
        harness.assertNotOnBattlefield(player1, "Deathpact Angel");
        harness.assertInGraveyard(player2, "Deathpact Angel");
    }

    @Test
    void angelIsChosenAtResolutionRatherThanActivation() {
        killAngel();
        Permanent cleric = readyCleric();
        harness.setGraveyard(player1, List.of());
        payForCleric();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null);
        harness.setGraveyard(player1, List.of(new DeathpactAngel()));
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, indexInGraveyard(player1, "Deathpact Angel"));

        harness.assertOnBattlefield(player1, "Deathpact Angel");
        harness.assertNotInGraveyard(player1, "Deathpact Angel");
    }

    @Test
    void abilityNeedsTwoBlackManaEvenWhenSixManaIsAvailable() {
        killAngel();
        Permanent cleric = readyCleric();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cleric");
        harness.assertInGraveyard(player1, "Deathpact Angel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenCanReturnADifferentAngelWithTheSameName() {
        killAngel();
        Permanent cleric = readyCleric();
        DeathpactAngel otherAngel = new DeathpactAngel();
        harness.setGraveyard(player1, List.of(otherAngel));
        payForCleric();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cleric), null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Deathpact Angel").getCard().getId()).isEqualTo(otherAngel.getId());
        harness.assertNotInGraveyard(player1, "Deathpact Angel");
    }

    private void payForCleric() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    /** Wrath of God cast by the opponent kills the Angel; both the death trigger and the token resolve. */
    private void killAngel() {
        harness.addToBattlefield(player1, new DeathpactAngel());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
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
