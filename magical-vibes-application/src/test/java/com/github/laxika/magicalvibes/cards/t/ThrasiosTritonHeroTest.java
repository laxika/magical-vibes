package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ThrasiosTritonHero.class, Forest.class, GrizzlyBears.class})
class ThrasiosTritonHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Ability resolves scry 1 before revealing the top card")
    void scriesBeforeReveal() {
        addReadyThrasios();
        Card topCard = new GrizzlyBears();
        Card nextCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    @Test
    @DisplayName("Revealed land enters the battlefield tapped")
    void putsRevealedLandOntoBattlefieldTapped() {
        addReadyThrasios();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        Permanent landPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == land)
                .findFirst()
                .orElseThrow();
        assertThat(landPermanent.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Revealed nonland card is drawn")
    void drawsRevealedNonland() {
        addReadyThrasios();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(nonland));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).contains(nonland);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(nonland);
    }

    private Permanent addReadyThrasios() {
        Permanent thrasios = addCreatureReady(player1, new ThrasiosTritonHero());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return thrasios;
    }
}
