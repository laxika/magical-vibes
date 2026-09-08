package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HoundOfTheFarbogsTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have menace without delirium")
    void noDeliriumNoMenace() {
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has menace with four card types in its controller's graveyard")
    void deliriumGrantsMenace() {
        setDelirium();
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's graveyard does not count toward delirium")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        assertThat(gqs.hasKeyword(gd, findHound(), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Loses menace when its controller's graveyard drops below four card types")
    void losesMenaceWhenGraveyardChanges() {
        setDelirium();
        harness.addToBattlefield(player1, new HoundOfTheFarbogs());

        Permanent hound = findHound();
        assertThat(gqs.hasKeyword(gd, hound, Keyword.MENACE)).isTrue();

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        assertThat(gqs.hasKeyword(gd, hound, Keyword.MENACE)).isFalse();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private Permanent findHound() {
        return findPermanent(player1, "Hound of the Farbogs");
    }
}
