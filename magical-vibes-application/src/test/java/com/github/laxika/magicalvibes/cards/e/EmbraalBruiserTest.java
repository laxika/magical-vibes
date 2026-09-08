package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmbraalBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped without menace when you control no artifact")
    void entersTappedWithoutMenace() {
        harness.setHand(player1, List.of(new EmbraalBruiser()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bruiser = findPermanent(player1, "Embraal Bruiser");
        assertThat(bruiser.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Has menace while you control an artifact")
    void hasMenaceWhileControllingArtifact() {
        harness.addToBattlefield(player1, new EmbraalBruiser());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent bruiser = findPermanent(player1, "Embraal Bruiser");
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Loses menace when you stop controlling artifacts")
    void losesMenaceWhenArtifactLeaves() {
        harness.addToBattlefield(player1, new EmbraalBruiser());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent bruiser = findPermanent(player1, "Embraal Bruiser");
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Spellbook"));

        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact does not grant menace")
    void opponentArtifactDoesNotGrantMenace() {
        harness.addToBattlefield(player1, new EmbraalBruiser());
        harness.addToBattlefield(player2, new Spellbook());

        Permanent bruiser = findPermanent(player1, "Embraal Bruiser");
        assertThat(gqs.hasKeyword(gd, bruiser, Keyword.MENACE)).isFalse();
    }
}
