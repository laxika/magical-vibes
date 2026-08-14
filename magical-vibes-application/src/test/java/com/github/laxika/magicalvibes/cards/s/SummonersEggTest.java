package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SummonersEggTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a card face down and imprint it")
    void etbImprintsCardFaceDown() {
        SummonersEgg eggCard = new SummonersEgg();
        GrizzlyBears imprintedCard = new GrizzlyBears();
        harness.setHand(player1, List.of(eggCard, imprintedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent egg = findPermanent(player1, "Summoner's Egg");
        assertThat(gd.getImprintedCard(egg.getCard())).isSameAs(imprintedCard);
        assertThat(gd.findExiledCard(imprintedCard.getId()).faceDown()).isTrue();
    }

    @Test
    @DisplayName("Death trigger turns an imprinted creature face up and puts it onto the battlefield")
    void deathTriggerReturnsImprintedCreatureToBattlefield() {
        SummonersEgg eggCard = new SummonersEgg();
        harness.addToBattlefield(player1, eggCard);

        GrizzlyBears imprintedCard = new GrizzlyBears();
        Permanent egg = findPermanent(player1, "Summoner's Egg");
        gd.setImprintedCard(egg.getCard(), imprintedCard);
        gd.addToExile(player1.getId(), imprintedCard, egg.getId(), true);

        destroyEgg(egg.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Summoner's Egg");
        assertThat(gd.findExiledCard(imprintedCard.getId())).isNull();
    }

    @Test
    @DisplayName("Death trigger leaves a noncreature imprinted card in exile")
    void deathTriggerLeavesNoncreatureInExile() {
        SummonersEgg eggCard = new SummonersEgg();
        harness.addToBattlefield(player1, eggCard);

        Card imprintedCard = new Spellbook();
        Permanent egg = findPermanent(player1, "Summoner's Egg");
        gd.setImprintedCard(egg.getCard(), imprintedCard);
        gd.addToExile(player1.getId(), imprintedCard, egg.getId(), true);

        destroyEgg(egg.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Summoner's Egg");
        assertThat(gd.findExiledCard(imprintedCard.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    private void destroyEgg(UUID eggId) {
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, eggId);
        harness.passBothPriorities();
    }
}
