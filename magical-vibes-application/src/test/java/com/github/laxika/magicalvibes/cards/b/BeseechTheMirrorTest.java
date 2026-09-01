package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RuneScarredDemon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeseechTheMirror.class, DarksteelRelic.class, GrizzlyBears.class, Island.class, RuneScarredDemon.class})
class BeseechTheMirrorTest extends BaseCardTest {

    @Test
    void withoutBargainPutsTheChosenCardIntoHand() {
        Card chosen = new GrizzlyBears();
        prepare(chosen);

        harness.castSorcery(player1, 0, 0);
        resolveAndChoose();

        assertInHand(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void bargainedEligibleCardIsExiledFaceDownAndMayBeCast() {
        Card chosen = new GrizzlyBears();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        prepare(chosen);

        castBargained(sacrifice);
        harness.passBothPriorities();
        chooseFirstCard();

        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    @Test
    void decliningEligibleFreeCastPutsTheCardIntoHand() {
        Card chosen = new GrizzlyBears();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        prepare(chosen);

        castBargained(sacrifice);
        resolveAndChoose();
        harness.handleMayAbilityChosen(player1, false);

        assertInHand(chosen);
        assertThat(gd.findExiledCard(chosen.getId())).isNull();
    }

    @Test
    void bargainedLandAndExpensiveCardGoToHandWithoutAnOffer() {
        for (Card chosen : List.of(new Island(), new RuneScarredDemon())) {
            Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
            prepare(chosen);

            castBargained(sacrifice);
            resolveAndChoose();

            assertInHand(chosen);
            assertThat(gd.findExiledCard(chosen.getId())).isNull();
            assertThat(gd.interaction.activeInteraction()).isNull();
        }
    }

    private void prepare(Card libraryCard) {
        harness.setHand(player1, List.of(new BeseechTheMirror()));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void resolveAndChoose() {
        harness.passBothPriorities();
        chooseFirstCard();
    }

    private void castBargained(Permanent sacrifice) {
        harness.getGameService().playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
    }

    private void chooseFirstCard() {
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));
    }

    private void assertInHand(Card card) {
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(handCard -> handCard.getId().equals(card.getId()));
    }
}
