package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasCommand.class, ArgothianSprite.class, EnergyRefractor.class})
class UrzasCommandTest extends BaseCardTest {

    @Test
    @DisplayName("The debuff and Powerstone modes affect only opposing creatures and create a tapped token")
    void debuffsOpposingCreaturesAndCreatesPowerstone() {
        Permanent ownCreature = addCreatureReady(player1, new ArgothianSprite());
        Permanent opposingCreature = addCreatureReady(player2, new ArgothianSprite());

        castWithModes(0, 1);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(0);
        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The Construct mode scales the token with artifacts you control")
    void constructScalesWithControlledArtifacts() {
        harness.addToBattlefield(player1, new EnergyRefractor());

        castWithModes(1, 2);

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(construct.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(construct.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(construct.getCard().getSubtypes()).containsExactly(CardSubtype.CONSTRUCT);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(3);
        assertThat(construct.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The scry mode draws after the scry choice completes")
    void scriesThenDraws() {
        Card topCard = new ArgothianSprite();
        harness.setLibrary(player1, List.of(topCard, new EnergyRefractor()));

        castWithModes(1, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertInHand(player1, "Argothian Sprite");
    }

    @Test
    @DisplayName("Bottoming the scry card draws the next card instead")
    void drawsNextCardAfterBottoming() {
        Card topCard = new ArgothianSprite();
        Card nextCard = new EnergyRefractor();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        castWithModes(2, 3);

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);
        assertThat(construct.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The Construct bonus updates when artifacts enter and ignores opposing artifacts")
    void constructBonusUpdatesWithControlledArtifacts() {
        harness.addToBattlefield(player2, new EnergyRefractor());
        castWithModes(0, 2);

        Permanent construct = findPermanent(player1, "Construct");
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(1);

        harness.addToBattlefield(player1, new EnergyRefractor());

        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff does not affect creatures entering after resolution")
    void debuffDoesNotAffectLaterCreatures() {
        Permanent existingCreature = addCreatureReady(player2, new ArgothianSprite());
        castWithModes(0, 1);

        Permanent laterCreature = addCreatureReady(player2, new ArgothianSprite());

        assertThat(gqs.getEffectivePower(gd, existingCreature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, existingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The debuff expires at the end of the turn")
    void debuffExpiresAtEndOfTurn() {
        Permanent opposingCreature = addCreatureReady(player2, new ArgothianSprite());
        harness.setLibrary(player1, List.of(new EnergyRefractor()));
        castWithModes(0, 3);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Choosing fewer than two modes is rejected")
    void rejectsFewerThanTwoModes() {
        harness.setHand(player1, List.of(new UrzasCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid mode bitmask");
    }

    private void castWithModes(int firstMode, int secondMode) {
        harness.setHand(player1, List.of(new UrzasCommand()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{firstMode, secondMode}, null, List.of());
        harness.passBothPriorities();
    }
}
