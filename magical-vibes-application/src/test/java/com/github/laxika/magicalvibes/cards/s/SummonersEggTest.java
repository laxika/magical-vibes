package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.d.DevourInShadow;
import com.github.laxika.magicalvibes.cards.g.GoblinBrawler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonersEgg.class, DevourInShadow.class, GoblinBrawler.class, ConjurersBauble.class})
class SummonersEggTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a card face down and imprint it")
    void etbImprintsCardFaceDown() {
        SummonersEgg eggCard = new SummonersEgg();
        GoblinBrawler imprintedCard = new GoblinBrawler();
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
    @DisplayName("ETB may be declined without exiling a card")
    void etbMayBeDeclined() {
        SummonersEgg eggCard = new SummonersEgg();
        GoblinBrawler imprintedCard = new GoblinBrawler();
        harness.setHand(player1, List.of(eggCard, imprintedCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Goblin Brawler");
        Permanent egg = findPermanent(player1, "Summoner's Egg");
        assertThat(gd.getImprintedCard(egg.getCard())).isNull();
        assertThat(gd.findExiledCard(imprintedCard.getId())).isNull();
    }

    @Test
    @DisplayName("Death trigger turns an imprinted creature face up and puts it onto the battlefield")
    void deathTriggerReturnsImprintedCreatureToBattlefield() {
        SummonersEgg eggCard = new SummonersEgg();
        harness.addToBattlefield(player1, eggCard);

        GoblinBrawler imprintedCard = new GoblinBrawler();
        Permanent egg = findPermanent(player1, "Summoner's Egg");
        gd.setImprintedCard(egg.getCard(), imprintedCard);
        gd.addToExile(player1.getId(), imprintedCard, egg.getId(), true);

        destroyEgg(egg.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goblin Brawler");
        harness.assertInGraveyard(player1, "Summoner's Egg");
        assertThat(gd.findExiledCard(imprintedCard.getId())).isNull();
    }

    @Test
    @DisplayName("Death trigger leaves a noncreature imprinted card in exile")
    void deathTriggerLeavesNoncreatureInExile() {
        SummonersEgg eggCard = new SummonersEgg();
        harness.addToBattlefield(player1, eggCard);

        Card imprintedCard = new ConjurersBauble();
        Permanent egg = findPermanent(player1, "Summoner's Egg");
        gd.setImprintedCard(egg.getCard(), imprintedCard);
        gd.addToExile(player1.getId(), imprintedCard, egg.getId(), true);

        destroyEgg(egg.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Summoner's Egg");
        assertThat(gd.findExiledCard(imprintedCard.getId()))
                .isNotNull()
                .extracting(exiledCard -> exiledCard.faceDown())
                .isEqualTo(false);
        harness.assertNotOnBattlefield(player1, "Conjurer's Bauble");
    }

    private void destroyEgg(UUID eggId) {
        harness.setHand(player2, List.of(new DevourInShadow()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, eggId);
        harness.passBothPriorities();
    }
}
