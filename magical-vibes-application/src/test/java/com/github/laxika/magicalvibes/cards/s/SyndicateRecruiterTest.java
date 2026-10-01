package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DigUpTheBody;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyndicateRecruiter.class, DigUpTheBody.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, Murder.class, Shock.class})
class SyndicateRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Mills four cards and conjures Dig Up the Body with five distinct graveyard mana values")
    void millsAndConjuresAtThreshold() {
        Card graveyardOne = new Forest();
        Card graveyardTwo = new Shock();
        Card graveyardThree = new GrizzlyBears();
        Card milledOne = new HillGiant();
        Card milledTwo = new Murder();
        Card milledThree = new Forest();
        Card milledFour = new Forest();
        harness.setGraveyard(player1, List.of(graveyardOne, graveyardTwo, graveyardThree));
        harness.setLibrary(player1, List.of(milledOne, milledTwo, milledThree, milledFour));
        harness.setHand(player1, List.of(new SyndicateRecruiter()));
        addRecruiterMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(graveyardOne, graveyardTwo, graveyardThree,
                        milledOne, milledTwo, milledThree, milledFour);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Dig Up the Body");
    }

    @Test
    @DisplayName("Does not conjure Dig Up the Body below five distinct graveyard mana values")
    void doesNotConjureBelowThreshold() {
        Card graveyardOne = new Forest();
        Card graveyardTwo = new Shock();
        Card graveyardThree = new GrizzlyBears();
        Card milledOne = new Forest();
        Card milledTwo = new Shock();
        Card milledThree = new GrizzlyBears();
        Card milledFour = new Forest();
        harness.setGraveyard(player1, List.of(graveyardOne, graveyardTwo, graveyardThree));
        harness.setLibrary(player1, List.of(milledOne, milledTwo, milledThree, milledFour));
        harness.setHand(player1, List.of(new SyndicateRecruiter()));
        addRecruiterMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(graveyardOne, graveyardTwo, graveyardThree,
                        milledOne, milledTwo, milledThree, milledFour);
    }

    private void addRecruiterMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
