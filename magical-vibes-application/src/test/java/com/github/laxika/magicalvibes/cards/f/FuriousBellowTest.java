package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.InscribedTablet;
import com.github.laxika.magicalvibes.cards.y.YavimayaIconoclast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FuriousBellow.class, YavimayaIconoclast.class, InscribedTablet.class})
class FuriousBellowTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target creature, grants first strike, and starts scry 1")
    void boostsGrantsFirstStrikeAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());

        harness.setHand(player1, List.of(new FuriousBellow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    @DisplayName("Scry 1 can put the top card on the bottom")
    void scriesOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());
        Card topCard = new YavimayaIconoclast();
        Card nextCard = new YavimayaIconoclast();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        harness.setHand(player1, List.of(new FuriousBellow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).get(0)).isSameAs(nextCard);
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());
        castFuriousBellow(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InscribedTablet());
        harness.setHand(player1, List.of(new FuriousBellow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Scry can keep the caster's top card when targeting an opponent's creature")
    void keepsTopCardOfCastersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());
        Card topCard = new InscribedTablet();
        Card nextCard = new YavimayaIconoclast();
        Card opponentTopCard = new InscribedTablet();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        castFuriousBellow(target);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Furious Bellow");
    }

    @Test
    @DisplayName("An empty library does not prevent the creature effects from resolving")
    void resolvesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YavimayaIconoclast());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FuriousBellow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Furious Bellow");
    }

    @Test
    @DisplayName("Does not scry when its only target has left the battlefield")
    void doesNotScryWithIllegalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YavimayaIconoclast());
        Card topCard = new InscribedTablet();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FuriousBellow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Furious Bellow");
    }
    private void castFuriousBellow(Permanent target) {
        harness.setHand(player1, List.of(new FuriousBellow()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
