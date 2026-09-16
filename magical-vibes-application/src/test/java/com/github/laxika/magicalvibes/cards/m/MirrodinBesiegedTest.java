package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrodinBesieged.class, Spellbook.class, Forest.class, GrizzlyBears.class})
class MirrodinBesiegedTest extends BaseCardTest {

    @Test
    @DisplayName("Mirran creates a Myr artifact creature when you cast an artifact")
    void mirranCreatesMyrOnArtifactCast() {
        castAndChoose("Mirran");

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Myr")
                        && permanent.getCard().isToken()
                        && permanent.getCard().hasType(CardType.ARTIFACT)
                        && permanent.getCard().hasType(CardType.CREATURE));
    }

    @Test
    @DisplayName("Phyrexian loots and makes the targeted opponent lose with fifteen artifact cards")
    void phyrexianLootsAndLosesGameAtArtifactThreshold() {
        castAndChoose("Phyrexian");
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, artifactGraveyard(15));

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest");
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Phyrexian does not create Myr or lose the game below the artifact threshold")
    void phyrexianDoesNotApplyMirranOrThresholdBranch() {
        castAndChoose("Phyrexian");

        harness.setHand(player1, List.of(new Spellbook(), new GrizzlyBears()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Myr"));

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, artifactGraveyard(14));

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.winnerPlayerId).isNull();
    }

    private void castAndChoose(String mode) {
        harness.setHand(player1, List.of(new MirrodinBesieged()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }

    private List<Card> artifactGraveyard(int count) {
        return IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new Spellbook())
                .toList();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
