package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TibaltsTrickeryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell, mills one to three cards, and offers the first different-name nonland")
    void countersMillsAndOffersDifferentNameCard() {
        GrizzlyBears target = new GrizzlyBears();
        Divination freeCast = new Divination();
        TibaltsTrickery trickery = new TibaltsTrickery();
        prepareTargetSpell(target);
        harness.setHand(player2, List.of(trickery));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new GrizzlyBears(), freeCast));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(target.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSizeBetween(1, 3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(freeCast);
        assertThat(gd.getCardsExiledByPermanent(trickery.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the free cast counters the target and returns all exiled cards to the library")
    void decliningFreeCastBottomsExiledCards() {
        GrizzlyBears target = new GrizzlyBears();
        TibaltsTrickery trickery = new TibaltsTrickery();
        prepareTargetSpell(target);
        harness.setHand(player2, List.of(trickery));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new GrizzlyBears(), new Divination()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(trickery.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSizeBetween(2, 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("If no different-name nonland is found, the exiled cards are bottomed and the spell is countered")
    void noDifferentNameCardBottomsEverything() {
        GrizzlyBears target = new GrizzlyBears();
        TibaltsTrickery trickery = new TibaltsTrickery();
        prepareTargetSpell(target);
        harness.setHand(player2, List.of(trickery));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getCardsExiledByPermanent(trickery.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSizeBetween(1, 3);
        assertThat(gd.stack).isEmpty();
    }

    private void prepareTargetSpell(Card target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
