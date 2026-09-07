package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChampionOfTheParish;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YouthfulValkyrie;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SigardaFontOfBlessings.class, Forest.class, ChampionOfTheParish.class,
        YouthfulValkyrie.class, GrizzlyBears.class})
class SigardaFontOfBlessingsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other permanents you control hexproof")
    void givesOtherPermanentsYouControlHexproof() {
        Permanent sigarda = harness.addToBattlefieldAndReturn(player1, new SigardaFontOfBlessings());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThat(gqs.hasKeyword(gd, ownForest, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, sigarda, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentForest, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Lets you cast a Human spell from the top of your library")
    void castsHumanFromTopOfLibrary() {
        addSigardaAndPrepareMainPhase();
        ChampionOfTheParish human = new ChampionOfTheParish();
        harness.setLibrary(player1, List.of(human));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(human.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(human);
    }

    @Test
    @DisplayName("Lets you cast an Angel spell from the top of your library")
    void castsAngelFromTopOfLibrary() {
        addSigardaAndPrepareMainPhase();
        YouthfulValkyrie angel = new YouthfulValkyrie();
        harness.setLibrary(player1, List.of(angel));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(angel.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(angel);
    }

    @Test
    @DisplayName("Does not let you cast another creature from the top of your library")
    void rejectsOtherCreatureFromTopOfLibrary() {
        addSigardaAndPrepareMainPhase();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    private void addSigardaAndPrepareMainPhase() {
        harness.addToBattlefield(player1, new SigardaFontOfBlessings());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
