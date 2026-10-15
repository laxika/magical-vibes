package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhituChronicler.class, Opt.class, Divination.class, PrimordialWurm.class})
class GhituChroniclerTest extends BaseCardTest {


    @Test
    @DisplayName("Cast without kicker — enters without an ETB trigger")
    void castWithoutKickerNoTrigger() {
        harness.setHand(player1, List.of(new GhituChronicler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        harness.assertOnBattlefield(player1, "Ghitu Chronicler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast without kicker — no graveyard interaction even with instant in graveyard")
    void castWithoutKickerIgnoresGraveyard() {
        harness.setGraveyard(player1, List.of(new Opt()));
        harness.setHand(player1, List.of(new GhituChronicler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Opt");
    }


    @Test
    @DisplayName("Cast with kicker — ETB trigger goes on the stack")
    void castWithKickerPutsEtbOnStack() {
        harness.setGraveyard(player1, List.of(new Opt()));
        castKicked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.assertOnBattlefield(player1, "Ghitu Chronicler");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cast with kicker — returns instant from graveyard to hand")
    void castWithKickerReturnsInstant() {
        Opt target = new Opt();
        harness.setGraveyard(player1, List.of(target));
        castKicked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertInHand(player1, "Opt");
        harness.assertNotInGraveyard(player1, "Opt");
    }

    @Test
    @DisplayName("Cast with kicker — returns sorcery from graveyard to hand")
    void castWithKickerReturnsSorcery() {
        Divination target = new Divination();
        harness.setGraveyard(player1, List.of(target));
        castKicked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Cast with kicker — does not offer non-instant/sorcery cards from graveyard")
    void castWithKickerDoesNotOfferCreature() {
        harness.setGraveyard(player1, List.of(new PrimordialWurm()));
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cast with kicker — empty graveyard, no graveyard choice")
    void castWithKickerEmptyGraveyard() {
        castKicked();
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }


    @Test
    @DisplayName("An opponent's graveyard cannot supply the target")
    void opponentGraveyardNotTargetable() {
        harness.setGraveyard(player2, List.of(new Opt(), new Divination()));
        castKicked();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Opt");
        harness.assertInGraveyard(player2, "Divination");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target that leaves the graveyard cannot be replaced during resolution")
    void missingTargetDoesNotReturnAnotherCard() {
        Opt target = new Opt();
        Divination other = new Divination();
        harness.setGraveyard(player1, List.of(target, other));
        castKicked();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Opt");
        harness.assertNotInHand(player1, "Divination");
        harness.assertInGraveyard(player1, "Divination");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Only the selected eligible card returns to hand")
    void returnsOnlySelectedCard() {
        Opt instant = new Opt();
        Divination sorcery = new Divination();
        harness.setGraveyard(player1, List.of(instant, sorcery, new PrimordialWurm()));
        castKicked();
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Divination");
        harness.assertNotInGraveyard(player1, "Divination");
        harness.assertInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player1, "Primordial Wurm");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castKicked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GhituChronicler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
    }
}
