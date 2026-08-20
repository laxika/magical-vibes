package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeliodGodOfTheSun;
import com.github.laxika.magicalvibes.cards.n.NyleaGodOfTheHunt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheWorldTreeTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new TheWorldTree()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "The World Tree").isTapped()).isTrue();
    }

    @Test
    void tapsForGreen() {
        Permanent tree = addReadyTree();

        harness.tapPermanent(player1, 0);

        assertThat(tree.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void givesOwnLandsAnyColorManaAtSixLands() {
        addReadyTree();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        Permanent fifthLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, 5, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(fifthLand.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opposingLand.isTapped()).isFalse();
    }

    @Test
    void searchesForAnyNumberOfGodsAfterSacrificing() {
        Permanent tree = addReadyTree();
        harness.setLibrary(player1, List.of(new HeliodGodOfTheSun(), new GrizzlyBears(), new NyleaGodOfTheHunt()));
        addSearchAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(tree.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("The World Tree");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Heliod, God of the Sun", "Nylea, God of the Hunt");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private Permanent addReadyTree() {
        Permanent tree = harness.addToBattlefieldAndReturn(player1, new TheWorldTree());
        tree.setSummoningSick(false);
        return tree;
    }

    private void addSearchAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
