package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PackGuardian.class, Forest.class})
class PackGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting may discards a land and creates a 2/2 green Wolf token")
    void acceptMayDiscardsLandAndCreatesWolf() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Forest landInHand = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new PackGuardian(), landInHand)));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → Pack Guardian enters
        harness.passBothPriorities(); // resolve ETB → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)
                .playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        assertThat(gd.stack).isEmpty();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).anyMatch(p ->
                p.getCard().isToken() && p.getCard().getName().equals("Wolf")
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().contains(CardSubtype.WOLF)
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("Declining may does not discard or create a token")
    void declineMayDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Forest landInHand = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new PackGuardian(), landInHand)));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        harness.assertNotOnBattlefield(player1, "Wolf");
    }

    @Test
    @DisplayName("Accepting may with no land in hand creates no token")
    void acceptMayWithNoLandDoesNothing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        PackGuardian nonlandInHand = new PackGuardian();
        harness.setHand(player1, new ArrayList<>(List.of(new PackGuardian(), nonlandInHand)));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        // No land to discard — no discard prompt, no token
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonlandInHand);
        harness.assertNotOnBattlefield(player1, "Wolf");
    }

    @Test
    @DisplayName("Discard choice only offers land cards")
    void discardChoiceOnlyOffersLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Forest land = new Forest();
        PackGuardian nonland = new PackGuardian();
        harness.setHand(player1, new ArrayList<>(List.of(new PackGuardian(), land, nonland)));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discard =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discard).isNotNull();
        // Hand after casting: [Forest, Pack Guardian] — only Forest (index 0) is valid
        assertThat(discard.validIndices()).containsExactly(0);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step")
    void canCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PackGuardian(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Pack Guardian");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Wolf");
    }

    @Test
    @DisplayName("An empty hand cannot pay for a Wolf token")
    void emptyHandCreatesNoWolf() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PackGuardian(), "{2}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Wolf");
    }
}
