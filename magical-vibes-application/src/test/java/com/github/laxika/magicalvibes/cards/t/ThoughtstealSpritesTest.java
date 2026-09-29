package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.b.BrazenBorrower;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FaebloomTrick;
import com.github.laxika.magicalvibes.cards.f.FaerieFencing;
import com.github.laxika.magicalvibes.cards.f.FaerieMastermind;
import com.github.laxika.magicalvibes.cards.f.FlitterwingNuisance;
import com.github.laxika.magicalvibes.cards.g.GlenElendraGuardian;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HypnoticSprite;
import com.github.laxika.magicalvibes.cards.l.LikenessLooter;
import com.github.laxika.magicalvibes.cards.p.Pestermite;
import com.github.laxika.magicalvibes.cards.p.PicklockPranksterFreeTheFae;
import com.github.laxika.magicalvibes.cards.s.SpellStutter;
import com.github.laxika.magicalvibes.cards.v.VendilionClique;
import com.github.laxika.magicalvibes.cards.v.VoraciousTomeSkimmer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtstealSprites.class, Bitterblossom.class, BrazenBorrower.class,
        FaebloomTrick.class, FaerieFencing.class, FaerieMastermind.class,
        FlitterwingNuisance.class, GlenElendraGuardian.class, HypnoticSprite.class,
        LikenessLooter.class, Pestermite.class, PicklockPranksterFreeTheFae.class,
        SpellStutter.class, VendilionClique.class, VoraciousTomeSkimmer.class,
        DarkRitual.class, GrizzlyBears.class})
class ThoughtstealSpritesTest extends BaseCardTest {

    @Test
    void castsDuringOpponentTurnDraftsThenDiscards() {
        harness.addToBattlefield(player1, new ThoughtstealSprites());
        Card discard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DarkRitual(), discard));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        enterOpponentTurn();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).hasSize(3);

        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discard));

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
    }

    @Test
    void triggersOnlyOnceDuringEachOpponentTurn() {
        harness.addToBattlefield(player1, new ThoughtstealSprites());
        Card discard = new GrizzlyBears();
        harness.setHand(player1, List.of(new DarkRitual(), discard, new DarkRitual()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);
        enterOpponentTurn();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(discard));

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerDuringControllerTurn() {
        harness.addToBattlefield(player1, new ThoughtstealSprites());
        harness.setHand(player1, List.of(new DarkRitual(), new GrizzlyBears()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void enterOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
