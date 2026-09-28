package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DirectorNickFury.class, DaredevilManWithoutFear.class, GrizzlyBears.class, MindStone.class})
class DirectorNickFuryTest extends BaseCardTest {

    @Test
    void heroSpellsCostOneLess() {
        harness.addToBattlefield(player1, new DirectorNickFury());
        harness.setHand(player1, List.of(new DaredevilManWithoutFear()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    void nonHeroSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new DirectorNickFury());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingLooksAtTopFourAndPutsARevealedHeroIntoHand() {
        addCreatureReady(player1, new DirectorNickFury());
        DaredevilManWithoutFear hero = new DaredevilManWithoutFear();
        Card nonHero = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hero, nonHero, new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(hero.getId());
        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(hero);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3).doesNotContain(hero);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
