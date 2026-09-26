package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.p.PerplexingChimera;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hinder.class, DevotedRetainer.class})
class HinderTest extends BaseCardTest {

    private DevotedRetainer prepareRetainerAndHinder() {
        DevotedRetainer retainer = new DevotedRetainer();
        harness.setLibrary(player1, List.of(new DevotedRetainer()));
        harness.setHand(player1, List.of(retainer));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Hinder()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, retainer.getId());
        return retainer;
    }

    private DevotedRetainer castRetainerAndHinder() {
        DevotedRetainer retainer = prepareRetainerAndHinder();
        harness.passBothPriorities();
        return retainer;
    }

    @Test
    @DisplayName("Choosing top puts the countered spell on top of its owner's library")
    void chooseTop() {
        DevotedRetainer retainer = castRetainerAndHinder();

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(retainer.getId());
        harness.assertNotInGraveyard(player1, "Devoted Retainer");
        harness.assertNotOnBattlefield(player1, "Devoted Retainer");
        harness.assertInGraveyard(player2, "Hinder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing bottom puts the countered spell on the bottom of its owner's library")
    void chooseBottom() {
        DevotedRetainer retainer = castRetainerAndHinder();

        harness.handleListChoice(player2, "Bottom");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getLast().getId()).isEqualTo(retainer.getId());
        assertThat(deck.getFirst().getId()).isNotEqualTo(retainer.getId());
        harness.assertNotInGraveyard(player1, "Devoted Retainer");
        harness.assertNotOnBattlefield(player1, "Devoted Retainer");
        harness.assertInGraveyard(player2, "Hinder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        prepareRetainerAndHinder();

        gd.stack.removeIf(se -> se.getCard().getName().equals("Devoted Retainer"));

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Hinder");
    }

    @Test
    @CardUsed(PerplexingChimera.class)
    @DisplayName("Choosing bottom uses the spell owner's library when control of the spell changed")
    void chooseBottomUsesSpellOwnersLibraryWhenSpellControllerDiffers() {
        DevotedRetainer retainer = new DevotedRetainer();
        harness.setLibrary(player1, List.of(new DevotedRetainer()));
        harness.setHand(player1, List.of(new Hinder(), retainer));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addToBattlefield(player2, new PerplexingChimera());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 1);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, retainer.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Bottom");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getLast().getId()).isEqualTo(retainer.getId());
        assertThat(deck.getFirst().getId()).isNotEqualTo(retainer.getId());
        harness.assertNotInGraveyard(player1, "Devoted Retainer");
        harness.assertInGraveyard(player1, "Hinder");
        assertThat(gd.stack).isEmpty();
    }
}
