package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.c.ChimericMass;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.cards.p.PalladiumMyr;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorHippogriff.class, GoldMyr.class, PalladiumMyr.class, Memnite.class,
        CarapaceForger.class, ChimericMass.class, OriginSpellbomb.class})
class RazorHippogriffTest extends BaseCardTest {

    /** Resolves the creature spell, leaving its enter trigger ready for target selection. */
    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new RazorHippogriff(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    /** Chooses the graveyard target before either player can respond to the trigger. */
    private void chooseTarget(Card card) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }

    @Test
    @DisplayName("Casting Razor Hippogriff puts it on the stack")
    void castingPutsOnStack() {
        RazorHippogriff hippogriff = new RazorHippogriff();
        harness.castFromHand(player1, hippogriff, "{3}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(hippogriff);
    }

    @Test
    @DisplayName("ETB returns artifact from graveyard to hand and gains life equal to mana value")
    void returnsArtifactAndGainsLife() {
        GoldMyr artifact = new GoldMyr();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.assertInGraveyard(player1, "Gold Myr");
        harness.assertNotInHand(player1, "Gold Myr");
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Gold Myr");
        harness.assertNotInGraveyard(player1, "Gold Myr");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB gains life based on returned card's mana value")
    void gainsLifeEqualToManaValue() {
        PalladiumMyr artifact = new PalladiumMyr();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Choosing specific artifact when multiple are in graveyard")
    void choosesSpecificArtifactFromGraveyard() {
        PalladiumMyr artifact = new PalladiumMyr();
        harness.setGraveyard(player1, List.of(new GoldMyr(), artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Palladium Myr");
        harness.assertInGraveyard(player1, "Gold Myr");
        harness.assertNotInGraveyard(player1, "Palladium Myr");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Returning a zero mana value artifact does not gain life")
    void zeroManaValueArtifactGainsNoLife() {
        Memnite artifact = new Memnite();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Memnite");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("No trigger remains on the stack without a legal graveyard target")
    void noEffectWithEmptyGraveyard() {
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Razor Hippogriff");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Non-artifact cards cannot supply the required graveyard target")
    void noEffectWithOnlyNonArtifactsInGraveyard() {
        harness.setGraveyard(player1, List.of(new CarapaceForger()));
        castAndResolve();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Carapace Forger");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot choose non-artifact card from graveyard")
    void cannotChooseNonArtifactFromGraveyard() {
        CarapaceForger nonArtifact = new CarapaceForger();
        GoldMyr artifact = new GoldMyr();
        harness.setGraveyard(player1, List.of(nonArtifact, artifact));
        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        chooseTarget(artifact);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Carapace Forger");
        harness.assertInHand(player1, "Gold Myr");
    }

    @Test
    @DisplayName("The mandatory artifact target cannot be declined")
    void cannotDeclineArtifactTarget() {
        GoldMyr artifact = new GoldMyr();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        chooseTarget(artifact);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Gold Myr");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Razor Hippogriff remains on battlefield after ETB resolves")
    void remainsOnBattlefieldAfterEtb() {
        GoldMyr artifact = new GoldMyr();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Razor Hippogriff");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterFullResolution() {
        GoldMyr artifact = new GoldMyr();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        GoldMyr artifact = new GoldMyr();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        chooseTarget(artifact);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Gold Myr");
    }

    @Test
    void returnsNoncreatureArtifact() {
        OriginSpellbomb artifact = new OriginSpellbomb();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Origin Spellbomb");
        harness.assertNotInGraveyard(player1, "Origin Spellbomb");
        harness.assertLife(player1, 21);
    }

    @Test
    void xInGraveyardManaCostCountsAsZero() {
        ChimericMass artifact = new ChimericMass();
        harness.setGraveyard(player1, List.of(artifact));
        castAndResolve();
        chooseTarget(artifact);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chimeric Mass");
        harness.assertLife(player1, 20);
    }

    @Test
    void illegalTargetDoesNotReturnAnotherArtifactOrGainLife() {
        GoldMyr target = new GoldMyr();
        PalladiumMyr other = new PalladiumMyr();
        harness.setGraveyard(player1, List.of(target, other));
        castAndResolve();
        chooseTarget(target);

        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Gold Myr");
        harness.assertNotInHand(player1, "Palladium Myr");
        harness.assertInGraveyard(player1, "Palladium Myr");
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsArtifactCannotBeTargeted() {
        GoldMyr own = new GoldMyr();
        PalladiumMyr opponents = new PalladiumMyr();
        harness.setGraveyard(player1, List.of(own));
        harness.setGraveyard(player2, List.of(opponents));
        castAndResolve();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponents.getId())))
                .isInstanceOf(IllegalStateException.class);
        chooseTarget(own);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Palladium Myr");
        harness.assertInHand(player1, "Gold Myr");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    void artifactArrivingAfterEntryCannotSupplyMissingTarget() {
        castAndResolve();
        harness.setGraveyard(player1, List.of(new GoldMyr()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Gold Myr");
        harness.assertNotInHand(player1, "Gold Myr");
        harness.assertLife(player1, 20);
    }
}
