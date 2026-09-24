package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverbackElder.class, AngelsFeather.class, Forest.class, GrizzlyBears.class})
class SilverbackElderTest extends BaseCardTest {

    private static final String DESTROY = "Destroy target artifact or enchantment.";
    private static final String LAND = "Look at the top five cards of your library. You may put a land card from among them onto the battlefield tapped. Put the rest on the bottom of your library in a random order.";
    private static final String LIFE = "You gain 4 life.";

    @Test
    @DisplayName("The destroy mode destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, DESTROY);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        harness.assertInGraveyard(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("The destroy mode cannot target a creature")
    void destroyModeRejectsCreatureTarget() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AngelsFeather());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, DESTROY);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The land mode puts a chosen land from the top five onto the battlefield tapped")
    void putsLandOntoBattlefieldTapped() {
        harness.addToBattlefield(player1, new SilverbackElder());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LAND);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(forest.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.selectedToBattlefieldTapped()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .allMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("The life mode gains four life")
    void gainsLife() {
        harness.addToBattlefield(player1, new SilverbackElder());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castCreatureAndResolveTrigger();

        harness.handleListChoice(player1, LIFE);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    private void castCreatureAndResolveTrigger() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
